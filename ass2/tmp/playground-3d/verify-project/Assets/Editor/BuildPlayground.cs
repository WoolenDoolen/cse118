using System.IO;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;

public static class BuildPlayground
{
    [InitializeOnLoadMethod]
    static void BuildOnFirstOpen()
    {
        if (!Application.isBatchMode && !File.Exists("Assets/Scenes/PhysicsPlayground.unity"))
            EditorApplication.delayCall += Build;
    }

    [InitializeOnLoadMethod]
    static void UpgradeOpenPlayground()
    {
        if (!Application.isBatchMode)
            EditorApplication.delayCall += TryUpgradeOpenPlayground;
    }

    static void TryUpgradeOpenPlayground()
    {
        Scene scene = SceneManager.GetActiveScene();
        if (scene.path != "Assets/Scenes/PhysicsPlayground.unity")
            return;
        Camera camera = Camera.main;
        if (camera == null || camera.GetComponent<PlaygroundCamera>() != null)
            return;
        if (EditorApplication.isPlayingOrWillChangePlaymode)
        {
            EditorApplication.playModeStateChanged -= UpgradeAfterPlay;
            EditorApplication.playModeStateChanged += UpgradeAfterPlay;
            EditorApplication.isPlaying = false;
            return;
        }
        Use3DView();
    }

    static void UpgradeAfterPlay(PlayModeStateChange state)
    {
        if (state != PlayModeStateChange.EnteredEditMode)
            return;
        EditorApplication.playModeStateChanged -= UpgradeAfterPlay;
        EditorApplication.delayCall += TryUpgradeOpenPlayground;
    }

    [MenuItem("Tools/Use 3D Playground View")]
    public static void Use3DView()
    {
        Scene scene = SceneManager.GetActiveScene();
        if (scene.path != "Assets/Scenes/PhysicsPlayground.unity" || EditorApplication.isPlaying)
            return;
        bool alreadyDirty = scene.isDirty;
        PlaygroundCamera.ApplyScenePresentation(scene);
        EditorSceneManager.MarkSceneDirty(scene);
        // Preserve any unsaved scene work instead of saving it implicitly.
        if (!alreadyDirty)
            EditorSceneManager.SaveScene(scene);
        Debug.Log("Physics Playground updated to the perspective 3D view.");
    }

    [MenuItem("Tools/Build Physics Playground")]
    public static void Build()
    {
        Directory.CreateDirectory("Assets/Scenes");
        Directory.CreateDirectory("Assets/Prefabs");
        Directory.CreateDirectory("Assets/Materials");
        AssetDatabase.Refresh();
        EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);

        Material floor = MakeMaterial("Floor", new Color(0.25f, 0.29f, 0.34f));
        Material wall = MakeMaterial("Walls", new Color(0.43f, 0.48f, 0.53f));
        Material playerColor = MakeMaterial("Player", new Color(0.95f, 0.95f, 1f));
        Material gold = MakeMaterial("Gold", new Color(1f, 0.72f, 0.05f));
        Material green = MakeMaterial("JumpPad", new Color(0.15f, 0.9f, 0.3f));
        Material purple = MakeMaterial("TeleportPad", new Color(0.65f, 0.25f, 1f));
        Material blue = MakeMaterial("SpawnButton", new Color(0.1f, 0.55f, 1f));
        Material brown = MakeMaterial("Crate", new Color(0.65f, 0.36f, 0.15f));

        new GameObject("Game Manager").AddComponent<GameManager>();
        Shape("Floor", PrimitiveType.Cube, new Vector3(0, -0.5f, 0), new Vector3(24, 1, 24), floor);
        Shape("North Wall", PrimitiveType.Cube, new Vector3(0, 0.5f, 12), new Vector3(25, 1, 1), wall);
        Shape("South Wall", PrimitiveType.Cube, new Vector3(0, 0.5f, -12), new Vector3(25, 1, 1), wall);
        Shape("West Wall", PrimitiveType.Cube, new Vector3(-12, 0.5f, 0), new Vector3(1, 1, 24), wall);
        Shape("East Wall", PrimitiveType.Cube, new Vector3(12, 0.5f, 0), new Vector3(1, 1, 24), wall);
        Shape("Raised Platform", PrimitiveType.Cube, new Vector3(0, 1.5f, 7), new Vector3(4, 3, 4), wall);

        GameObject player = Shape("Player", PrimitiveType.Sphere, new Vector3(0, 0.65f, -8), Vector3.one, playerColor);
        player.layer = 2; // Ignore Raycast, so the ground ray cannot hit the player itself.
        Rigidbody playerBody = player.AddComponent<Rigidbody>();
        playerBody.constraints = RigidbodyConstraints.FreezeRotation;
        playerBody.interpolation = RigidbodyInterpolation.Interpolate;
        playerBody.collisionDetectionMode = CollisionDetectionMode.ContinuousDynamic;
        player.AddComponent<PlayerController>();
        SaveAndConnect(player, "Player");

        GameObject pickup = Shape("Gold Pickup", PrimitiveType.Cube, Vector3.zero, Vector3.one * 0.7f, gold);
        pickup.GetComponent<Collider>().isTrigger = true;
        pickup.AddComponent<Pickup>();
        GameObject pickupPrefab = SavePrefab(pickup, "GoldPickup");
        Object.DestroyImmediate(pickup);
        Vector3[] pickupPositions = {
            new Vector3(-7, 0.8f, -5), new Vector3(7, 0.8f, -5),
            new Vector3(-7, 0.8f, 7), new Vector3(7, 0.8f, 7), new Vector3(0, 3.8f, 7)
        };
        foreach (Vector3 position in pickupPositions)
            PlacePrefab(pickupPrefab, position);

        GameObject jump = Pad("Jump Pad", new Vector3(0, 0.1f, 3), green);
        jump.AddComponent<JumpPad>();
        SaveAndConnect(jump, "JumpPad");
        Label("JUMP", new Vector3(0, 0.15f, 1.8f));

        GameObject teleporter = Pad("Teleport Pad", Vector3.zero, purple);
        teleporter.AddComponent<TeleportPad>();
        GameObject teleportPrefab = SavePrefab(teleporter, "TeleportPad");
        Object.DestroyImmediate(teleporter);
        GameObject leftPad = PlacePrefab(teleportPrefab, new Vector3(-8, 0.1f, 2));
        GameObject rightPad = PlacePrefab(teleportPrefab, new Vector3(8, 0.1f, 2));
        leftPad.GetComponent<TeleportPad>().destination = rightPad.transform;
        rightPad.GetComponent<TeleportPad>().destination = leftPad.transform;
        PrefabUtility.RecordPrefabInstancePropertyModifications(leftPad.GetComponent<TeleportPad>());
        PrefabUtility.RecordPrefabInstancePropertyModifications(rightPad.GetComponent<TeleportPad>());
        Label("TELEPORT", new Vector3(-8, 0.15f, 0.8f));
        Label("TELEPORT", new Vector3(8, 0.15f, 0.8f));

        GameObject crate = Shape("Physics Crate", PrimitiveType.Cube, Vector3.zero, Vector3.one, brown);
        crate.AddComponent<Rigidbody>();
        crate.AddComponent<CrateLifetime>();
        GameObject cratePrefab = SavePrefab(crate, "PhysicsCrate");
        Object.DestroyImmediate(crate);
        PlacePrefab(cratePrefab, new Vector3(-3, 1, -3));
        PlacePrefab(cratePrefab, new Vector3(3, 1, -3));

        GameObject button = Pad("Spawn Button", new Vector3(-5, 0.1f, -8), blue);
        SpawnButton spawnButton = button.AddComponent<SpawnButton>();
        spawnButton.cratePrefab = cratePrefab;
        spawnButton.spawnPoint = new GameObject("Crate Spawn Point").transform;
        spawnButton.spawnPoint.position = new Vector3(-5, 3, -5.5f);
        Label("SPAWN (E)", new Vector3(-5, 0.15f, -9.3f));

        GameObject cameraObject = new GameObject("Main Camera");
        cameraObject.tag = "MainCamera";
        Camera camera = cameraObject.AddComponent<Camera>();
        cameraObject.AddComponent<AudioListener>();
        camera.transform.position = new Vector3(0, 18, -24);
        camera.transform.LookAt(new Vector3(0, 0, 1));
        camera.orthographic = false;
        camera.fieldOfView = 44;
        camera.clearFlags = CameraClearFlags.SolidColor;
        camera.backgroundColor = new Color(0.12f, 0.15f, 0.2f);

        Light light = new GameObject("Sun").AddComponent<Light>();
        light.type = LightType.Directional;
        light.intensity = 1.2f;
        light.transform.rotation = Quaternion.Euler(50, -30, 0);
        PlaygroundCamera.ApplyScenePresentation(SceneManager.GetActiveScene());
        PlayerSettings.companyName = "CMPM125";
        PlayerSettings.productName = "Physics Playground";
        PlayerSettings.defaultScreenWidth = 1280;
        PlayerSettings.defaultScreenHeight = 800;

        const string scenePath = "Assets/Scenes/PhysicsPlayground.unity";
        EditorSceneManager.SaveScene(SceneManager.GetActiveScene(), scenePath);
        EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(scenePath, true) };
        AssetDatabase.SaveAssets();
        Debug.Log("Physics Playground created successfully: " + scenePath);
    }

    static Material MakeMaterial(string name, Color color)
    {
        string path = "Assets/Materials/" + name + ".mat";
        Material material = AssetDatabase.LoadAssetAtPath<Material>(path);
        if (material == null)
        {
            material = new Material(Shader.Find("Standard"));
            AssetDatabase.CreateAsset(material, path);
        }
        material.color = color;
        return material;
    }

    static GameObject Shape(string name, PrimitiveType type, Vector3 position, Vector3 scale, Material material)
    {
        GameObject obj = GameObject.CreatePrimitive(type);
        obj.name = name;
        obj.transform.position = position;
        obj.transform.localScale = scale;
        obj.GetComponent<Renderer>().sharedMaterial = material;
        return obj;
    }

    static GameObject Pad(string name, Vector3 position, Material material)
    {
        GameObject pad = Shape(name, PrimitiveType.Cube, position, new Vector3(2, 0.2f, 2), material);
        BoxCollider trigger = pad.GetComponent<BoxCollider>();
        trigger.isTrigger = true;
        trigger.size = new Vector3(1, 4, 1);
        return pad;
    }

    static GameObject SavePrefab(GameObject obj, string name)
    {
        return PrefabUtility.SaveAsPrefabAsset(obj, "Assets/Prefabs/" + name + ".prefab");
    }

    static void SaveAndConnect(GameObject obj, string name)
    {
        PrefabUtility.SaveAsPrefabAssetAndConnect(obj, "Assets/Prefabs/" + name + ".prefab", InteractionMode.AutomatedAction);
    }

    static GameObject PlacePrefab(GameObject prefab, Vector3 position)
    {
        GameObject obj = (GameObject)PrefabUtility.InstantiatePrefab(prefab);
        obj.transform.position = position;
        return obj;
    }

    static void Label(string text, Vector3 position)
    {
        TextMesh label = new GameObject(text + " Label").AddComponent<TextMesh>();
        label.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
        label.GetComponent<Renderer>().sharedMaterial = label.font.material;
        label.text = text;
        label.fontSize = 48;
        label.characterSize = 0.09f;
        label.anchor = TextAnchor.MiddleCenter;
        label.color = Color.white;
        label.transform.position = position;
        label.transform.rotation = Quaternion.Euler(55, 0, 0);
    }
}
