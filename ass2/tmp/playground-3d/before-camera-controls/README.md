# Physics Playground

A small Unity 6 game for Week 2: Physics World. All graphics are primitive shapes.
The fixed perspective camera, solid raised platform, and real-time shadows make height and landing positions visible.
The camera fits the arena when the Game view changes size, and the HUD sits above the play area.

## Open and play

1. In Unity Hub, choose **Add > Add project from disk**, then select this `ass2` folder.
2. Open with Unity **6000.6.3f1**, the newest version installed on this computer.
3. The level, materials, and prefabs are created automatically on the first open.
   Press **Play**. Later, open `Assets/Scenes/PhysicsPlayground.unity` to return to the level.
4. Click inside the Game view so the keyboard controls work.

Move with **WASD / arrow keys**, jump with **Space**, and restart with **R**.
Collect all **five gold cubes** to win. Walk over the **green jump pad**, then move
forward onto the raised platform to get the high cube. **Purple pads** teleport
between the sides. Stand on the **blue button** and press **E** to spawn crates.
You can push crates; they disappear after 12 seconds.

Unity Hub needs an active license. If prompted, sign in and activate the free
Personal license (if eligible) under **Settings > Licenses**.

If the first-open setup did not run, or you need to recreate the scene, use
**Tools > Build Physics Playground**.
This replaces the current scene with the original level.
To update only the camera, lighting, and platform presentation of an existing level,
use **Tools > Use 3D Playground View** outside Play mode. The original scene also upgrades automatically after script import.

## Assignment requirements

| Requirement | Implementation |
| --- | --- |
| Rigidbody or Character Controller movement | `PlayerController.cs`: a sphere with a Rigidbody |
| Multiple physics interactions | Jump pads, teleport pads, spawn button, collectible triggers, pushable crates |
| Raycasting | `PlayerController.cs`: downward `Physics.Raycast` checks the ground before jumping |
| Multiple prefab types used in scene | Player, GoldPickup, JumpPad, TeleportPad, PhysicsCrate in `Assets/Prefabs` |
| Instantiate and Destroy | `SpawnButton.cs` instantiates crates; `CrateLifetime.cs` destroys them; `Pickup.cs` destroys collected cubes |

## Submission

Record a short gameplay video: show movement and jumping, both pad types, pressing
E to create a crate, a crate disappearing, and collecting the five cubes to win.
Submit the video and `Assets/Scripts` folder as requested by the assignment.
If using Google Drive, enable sharing for the submission link.
