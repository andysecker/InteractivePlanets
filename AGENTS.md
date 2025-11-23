# Repository Guidelines

## Project Structure & Module Organization
- Source: `src/main/java/com/example/PlanetFormationDemo/` — core modules (`Simulation`, `RenderPanel`, `ControlBar`, `InputBinder`, managers, styling/util).
- Tests: `src/test/java/` (currently minimal; mirror package paths).
- Build output: `target/` (ignored).
- Docs: `README.md` (overview), `handoff.md` (recent context).

## Build, Test, and Development Commands
- Build (CLI): `mvn -q -DskipTests compile` (Java 25; clear stale `~/.m2/*.part.lock` if resolution fails).
- Run (CLI with GUI): `mvn -q compile exec:java -Dexec.mainClass=com.example.PlanetFormationDemo.PlanetFormationDemo`.
- Eclipse: run `PlanetFormationDemo` main class directly.

## Coding Style & Naming Conventions
- Language: Java; 4-space indent; PascalCase classes, camelCase members/methods, ALL_CAPS constants.
- Packages: `com.example.PlanetFormationDemo`.
- Keep UI, simulation, and utilities separated as in current modules; avoid leaving `.class` files in `src/` (use `target/`).

## Testing Guidelines
- Framework: JUnit (extend as features grow).
- Naming: tests as `*Test.java` under `src/test/java` mirroring packages.
- Run: `mvn test`.

## Commit & Pull Request Guidelines
- Commits: concise, imperative (e.g., “Randomize planet names”, “Draw comet tails view-only”). You can use `git commit -F git-commit-msg.txt`.
- PRs: include a short summary, describe UI/behavior changes (screenshots/gifs if applicable), and link issues when relevant.

## Architecture Notes
- Simulation emits snapshots/events; RenderPanel only renders (view-only zoom).
- ControlBar/InputBinder handle UI/input; Caption/Sound managers respond to events.
- Auto-comets default ON; mouse wheel zoom does not affect physics.

## Other
- Project: A fun personal project showing the way the solar system emerged. This is a fun demo to show to my 4-year old daughter.
- Your user has 25 years Java back end experience. User may make updates or fixes to the code between prompts.
- Sandbox expectations: Workspace-write; network restricted; no dangerous commands without approval.
- You are working in an Eclipse project
- Use libraries available on Maven to help where useful to do so
- Regularly update a document handoff.md in the root project directory which always has a (brief) summary of what we've been most recently working on, including my last couple of prompts. The goal is that if the context window gets too crowded, we can restart with a new task, and the new agent can pick up where you left off using the readme (describing the project) and the handoff document (describing what we were most recently working on). 

Codex session ID - 019aa199-4d08-7310-ba9c-60cea6d6b48b