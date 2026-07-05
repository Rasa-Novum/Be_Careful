# Harder Nether 0.5.1 Changelog:

### Features:

- Added Deep Dark corruption
  - Timer increments per player in the Deep Dark, sending a warning message at the specified time before damaging the player constantly after the danger time has passed.
- Added Totem of Light
  - Totem of Light cleanses Deep Dark corruption effect for user and players in the surrounding area
- Added Nether burn upon entry
  - Can be dealt with by placing new Frozen Campfires, obtained from snowy biomes.
- Added Fire Resistance to certain foods as an additonal way to combat the persistent burn in the Nether
  - Configurable, TBD
- Added gamerule to only allow ignition of nether portals in ruined portal structures.
- Changed Nether to Overworld scale to be 1:1 instead of vanilla 1:8.
- Inverted regional difficulty system such that chunks start in their hardest state and become easier over time.
- Added a gamerule to control the amount of time (in ticks) required to "tame" a chunk to allow for a player to sleep in a bed in said chunk.
- Added freezing effect to snowy biomes which can be avoided by staying near warmth or carrying a torch.
- Changed player End spawn to randomly spawn out within a configurable radius
- F3 coordinates are blocked for the player in the End, the player must use the reworked Ender Eye to locate the main dragon island
  - Small obsidian gateways spawn around the void perimeter that surrounds the dragon island so that the player does not have to bridge.
- Strongholds are removed, access to the End is granted via the portal in an Ancient City via an item dropped from killing the Warden.

### Changes:

- Added frozen campfire
  - frozen campfire consumes fuel like a campfire does food (displaying visually spare/not in use fuel) while ticking fuel timers down like a furnace would.
- Added breath effect in cold biomes or in range of frozen campfire
- Added freeze timer tracking and damage after time exceeded
- Made entire mod configurable by section of content such that the player can disable or enable what they want.
- removed nether hunger drain effect

### Notes: 

- some item names may change, textures are not implemented yet.
- some visuals (such as breath) are subject to change