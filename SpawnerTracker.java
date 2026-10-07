package com.spawnerbeacon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;

/** Merkt sich alle geladenen Spawner und baut daraus die Strahlen. */
public final class SpawnerTracker {
	/** Eintrag fuer die "Gefunden"-Liste im Menue. */
	public record SpawnerInfo(String type, int x, int y, int z, double distance) {
	}

	private static final Set<SpawnerBlockEntity> SPAWNERS = ConcurrentHashMap.newKeySet();
	private static final Set<String> ANNOUNCED = ConcurrentHashMap.newKeySet();
	private static volatile List<SpawnerInfo> found = List.of();

	private SpawnerTracker() {
	}

	public static List<SpawnerInfo> found() {
		return found;
	}

	public static void register() {
		ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof SpawnerBlockEntity spawner) {
				SPAWNERS.add(spawner);
			}
		});
		ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof SpawnerBlockEntity spawner) {
				SPAWNERS.remove(spawner);
			}
		});
	}

	public static String typeName(String type) {
		if (type.equals("other")) {
			return Component.translatable("sb.type.other").getString();
		}
		return Component.translatable("entity.minecraft." + type).getString();
	}

	public static List<BeamState> collect() {
		BeaconConfig cfg = BeaconConfig.get();
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || mc.player == null) {
			found = List.of();
			return List.of();
		}

		Vec3 pp = mc.player.position();
		long now = System.currentTimeMillis();
		float half = (float) (cfg.thickness / 2.0);

		List<BeamState> out = new ArrayList<>();
		List<SpawnerInfo> infos = new ArrayList<>();
		List<String> fresh = new ArrayList<>();

		Iterator<SpawnerBlockEntity> it = SPAWNERS.iterator();
		while (it.hasNext()) {
			SpawnerBlockEntity be = it.next();
			if (be.isRemoved() || be.getLevel() != level) {
				it.remove();
				continue;
			}
			BlockPos pos = be.getBlockPos();
			String type = typeOf(be, level, pos);
			double dx = pos.getX() + 0.5 - pp.x;
			double dy = pos.getY() + 0.5 - pp.y;
			double dz = pos.getZ() + 0.5 - pp.z;
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			infos.add(new SpawnerInfo(type, pos.getX(), pos.getY(), pos.getZ(), dist));

			if (!cfg.enabled || !cfg.isTypeEnabled(type) || dist > cfg.maxDistance) {
				continue;
			}

			String key = level.dimension() + "@" + pos.asLong();
			if (ANNOUNCED.add(key)) {
				fresh.add(typeName(type) + " (" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + ")");
			}

			int rgb;
			if (cfg.rainbow) {
				float hue = (float) ((now / 4000.0 + (pos.getX() + pos.getZ()) * 0.002) % 1.0);
				rgb = ColorState.hsvToRgb(hue, 0.75f, 1f);
			} else {
				rgb = cfg.colorFor(type);
			}
			float r = ((rgb >> 16) & 0xFF) / 255f;
			float g = ((rgb >> 8) & 0xFF) / 255f;
			float b = (rgb & 0xFF) / 255f;

			float alpha = (float) cfg.opacity;
			if (cfg.distanceFade) {
				alpha *= (float) Math.max(0.2, 1.0 - 0.65 * (dist / cfg.maxDistance));
			}
			if (cfg.pulse) {
				alpha *= (float) (0.8 + 0.2 * Math.sin(now / 350.0));
			}
			alpha = Math.max(0.03f, Math.min(1f, alpha));

			double cx = pos.getX() + 0.5;
			double cz = pos.getZ() + 0.5;

			if (cfg.maxY > pos.getY()) {
				out.add(new BeamState(cx, pos.getY(), cz, cfg.maxY, half, r, g, b, alpha));
				if (cfg.showCore) {
					float cr = r + (1f - r) * 0.55f;
					float cg = g + (1f - g) * 0.55f;
					float cb = b + (1f - b) * 0.55f;
					out.add(new BeamState(cx, pos.getY(), cz, cfg.maxY, half * 0.35f, cr, cg, cb,
							Math.min(1f, alpha + 0.3f)));
				}
			}
			if (cfg.blockHighlight) {
				out.add(new BeamState(cx, pos.getY() - 0.01, cz, pos.getY() + 1.01, 0.51f, r, g, b,
						Math.min(1f, alpha * 0.9f)));
			}
		}

		infos.sort(Comparator.comparingDouble(SpawnerInfo::distance));
		found = List.copyOf(infos);

		if (!fresh.isEmpty()) {
			notifyNew(mc, cfg, fresh);
		}
		return List.copyOf(out);
	}

	private static void notifyNew(Minecraft mc, BeaconConfig cfg, List<String> fresh) {
		if (cfg.notifyChat && mc.player != null) {
			int shown = Math.min(3, fresh.size());
			for (int i = 0; i < shown; i++) {
				mc.player.sendSystemMessage(Component.translatable("sb.msg.found", fresh.get(i)));
			}
			if (fresh.size() > shown) {
				mc.player.sendSystemMessage(Component.translatable("sb.msg.more", fresh.size() - shown));
			}
		}
		if (cfg.notifySound) {
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.3f));
		}
	}

	/** Liefert z. B. "zombie" oder "other", wenn die Art nicht bestimmt werden kann. */
	private static String typeOf(SpawnerBlockEntity be, ClientLevel level, BlockPos pos) {
		try {
			Entity e = be.getSpawner().getOrCreateDisplayEntity(level, pos);
			if (e != null) {
				return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
			}
		} catch (Exception ignored) {
			// faellt unten auf "other" zurueck
		}
		return "other";
	}
}
