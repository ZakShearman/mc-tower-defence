# MC Tower Defence
![License](https://img.shields.io/github/license/towerdefence-cc/tower-defence)

A tower defence plugin inspired by Cubecraft's tower defence and Bloons TD

## Project Status

A live **ALPHA** version is availabe on emortal.dev. It is still under heavy development and not ready for release.
Feel free to use the project for fun and experimentation.

## Requirements

  - Java 21+
  - 256MB RAM (recommended 512MB)

## Running Locally

You can run TowerDefence locally! This is only intended for development usage so it functions a bit differently but you can totally play locally.

1. Clone the project
2. Open the project in your IDE
3. Create a run configuration to run the class `pink.zak.minestom.towerdefence.TowerDefenceServer`. Set these environment variables: `ENABLE_TEST_MODE=false`. If you are in IntelliJ a run configuration is automatically created for you.
4. Run the server and execute `/forcestart` to start the game.

⚠️ Some features may be missing as they depend on our production servers (e.g. player data saving and game history)
⚠️ You will see quite a few things being logged as disabled. These are other features part of our core production system. That's fine.
