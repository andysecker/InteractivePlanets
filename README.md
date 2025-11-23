# Planet Formation Playground

A playful Swing demo of a protoplanetary disk: dust orbits a warm sun, bumps together, and grows into baby planets. Built to show a 4-year-old how planets form, with simple tools, comets, captions, and synthesized sounds.

## What’s Inside
- **Simulation**: physics, collisions, auto-comets, naming/stages, events.
- **RenderPanel**: draws bodies, HUD, labels; view-only zoom via mouse wheel.
- **ControlBar**: toolbar for tools/comets/shake/cleanup/auto-comet toggle.
- **InputBinder**: keyboard/mouse bindings for tools and toggles.
- **Managers**: `CaptionManager` (kid-friendly captions), `SoundManager` + `Sound` (whoosh/chime synth).
- **Data/Styling**: `Body`, `Stage`, `ToolMode`, `SimulationSnapshot`, `PlanetStyling` (colors/names), `ToolModeHolder`, `FormatUtil`.

## Run It
- **Eclipse**: run `com.example.PlanetFormationDemo.PlanetFormationDemo`.
- **CLI** (with Maven on PATH and GUI available):  
  `mvn -q compile exec:java -Dexec.mainClass=com.example.PlanetFormationDemo.PlanetFormationDemo`

> Note: Swing needs a display; headless shells will throw `HeadlessException`.

## Controls
- Tools: `1` Star wand, `2` Wind, `3` Gravity glove.
- Toolbar buttons: Make comet, Shake disk, Clean up dust, Toggle auto-comets.
- Mouse: Click/drag applies current tool; **mouse wheel zooms view** (non-destructive).
- ESC closes the app.

## Behavior Highlights
- Planets keep their names on merges; labels show name + particle count after 5k particles.
- Color stages: rock → ocean → garden → mystery at lower thresholds for variety.
- Auto-comets (12–24s interval) on by default; toggle via toolbar or `C`.
- Captions react to events (new planet, stage change, dense dust, comets) with friendly text.
- Sounds: synthesized whoosh on comets, chime on meaningful merges (gated to avoid flood).
- Sun/disk recenters on resize; bodies fill the window at startup.

## Build Notes
- Maven compiler set to **Java 25**; adjust `pom.xml` if needed for your JDK.
- If Maven fails on `*.part.lock` in `~/.m2`, delete stale lockfiles and retry.
- Build output should go to `target/`; source tree should not contain `.class` files (`.gitignore` provided).

## Files of Interest
- `src/main/java/com/example/PlanetFormationDemo/PlanetFormationDemo.java` — Bootstrap UI.
- `Simulation.java`, `RenderPanel.java`, `ControlBar.java`, `InputBinder.java` — core components.
- `CaptionManager.java`, `SoundManager.java`, `Sound.java` — captions/sounds.
- `PlanetStyling.java`, `FormatUtil.java`, `Body.java`, `Stage.java`, `ToolMode.java`, `ToolModeHolder.java`, `SimulationSnapshot.java` — data/styling/utilities.

Enjoy exploring how “space dust” turns into “planet babies!” 🎈
