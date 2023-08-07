package de.jakobkruse.tikfinity;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = TikfinityMod.MOD_ID)
class ModConfig implements ConfigData {
    int port = 4567;
}