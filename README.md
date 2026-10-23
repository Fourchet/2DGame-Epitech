# 2D GAME

A 2D top-down horror survival shooter made in Java with [libGDX](https://libgdx.com/).

You play as O'Connor, looking for his brother in a town overrun by zombies and soldiers.
Explore the maps, shoot your way through and survive.

## Features

- Free movement and mouse aiming, two weapons (pistol and rifle) with ammo and reload
- Two enemy types: **zombies** (melee) and **soldiers** (ranged or melee)
- Several maps made with Tiled, linked by doors and a car cutscene
- Dynamic lights and shadows, day / night cycle by sleeping in a bed
- Hand-made pixel art animations, blood effects and positional sounds
- Game over screen when the player dies

## Requirements

- JDK 17 or newer (tested with JDK 21)
- Nothing else: the Gradle wrapper (`./gradlew`) downloads what it needs

## Run the game

```bash
./gradlew lwjgl3:run
```

Build a runnable jar (created in `lwjgl3/build/libs/`):

```bash
./gradlew lwjgl3:jar
```

## Controls

| Action                    | QWERTY        | AZERTY        |
| ------------------------- | ------------- | ------------- |
| Move                      | W A S D       | Z Q S D       |
| Aim                       | Mouse         | Mouse         |
| Shoot / reload when empty | Left click    | Left click    |
| Pistol                    | 1             | &             |
| Rifle                     | 2             | é             |
| Switch weapon             | Q             | A             |
| Sleep (on a bed)          | E             | E             |
| Skip cutscene             | Space / Enter | Space / Enter |
| Restart after game over   | Enter         | Enter         |

## Tests and code coverage

Unit tests use JUnit 5, coverage uses JaCoCo.

```bash
./gradlew core:test
```

- Test results: `core/build/reports/tests/test/index.html`
- Coverage report: `docs/coverage/index.html` (generated after the tests)

## Documentation

- **Javadoc**: `docs/javadoc/index.html`, generated with `./gradlew core:javadoc`
- **Game Design Document**: `1.1v_GDD/`

## Project structure

```
core/src/main/java/com/mygame/
├── Main.java, GameScreen.java   entry point and game loop
├── entities/     Entity, Enemy, Zombie, Military, Player, Bullet
├── items/        Items, Weapon, Pistol, Rifle, MilitaryWeapon
├── factory/      EnemiesManager, BulletManager
├── collisions/   GameContactListener (Box2D contacts → damage)
├── world/        maps, spawns, doors, sleep event, cutscene
├── system/       camera, weapon controller, BulletOwner
├── render/       renderers, lights, post-processing shader
└── audio/        SoundManager
core/src/test/    JUnit tests
lwjgl3/           desktop launcher
assets/           sprites, sounds, Tiled maps, shaders
```

Main class hierarchy:

```
Entity (abstract)          Items (abstract)
├── Player                 └── Weapon (abstract)
└── Enemy (abstract)           ├── Pistol
    ├── Zombie                 ├── Rifle
    └── Military               └── MilitaryWeapon
```

## Built with

- [libGDX](https://libgdx.com/) - graphics, input, audio
- [Box2D](https://box2d.org/) - physics and collisions
- [box2dlights](https://github.com/libgdx/box2dlights) - lights and shadows
- [Tiled](https://www.mapeditor.org/) - map editor
