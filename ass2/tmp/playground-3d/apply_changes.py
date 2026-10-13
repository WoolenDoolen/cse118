from pathlib import Path
import hashlib
import shutil

project = Path(r'C:\Users\joust\OneDrive\Desktop\Code\cmpm125\ass2').resolve()
stage = Path(__file__).resolve().parent
backup = stage / 'original'
assert (project / 'Assets/Editor/BuildPlayground.cs').is_file()
for name in ['Assets/Scripts/GameManager.cs', 'Assets/Editor/BuildPlayground.cs', 'README.md']:
    target = backup / name
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(project / name, target)

builder = (project / 'Assets/Editor/BuildPlayground.cs').read_text()
assert 'camera.orthographic = true;' in builder
assert 'new Vector3(0, 2.5f, 7), new Vector3(4, 1, 4)' in builder
builder = builder.replace('new Vector3(0, 2.5f, 7), new Vector3(4, 1, 4)',
                          'new Vector3(0, 1.5f, 7), new Vector3(4, 3, 4)')
builder = builder.replace('camera.transform.position = new Vector3(0, 24, -24);',
                          'camera.transform.position = new Vector3(0, 18, -24);')
builder = builder.replace('camera.orthographic = true;\n        camera.orthographicSize = 17;',
                          'camera.orthographic = false;\n        camera.fieldOfView = 44;')
builder = builder.replace('RenderSettings.ambientLight = new Color(0.55f, 0.55f, 0.6f);',
                          'PlaygroundCamera.ApplyScenePresentation(SceneManager.GetActiveScene());')
marker = '    [MenuItem("Tools/Build Physics Playground")]'
upgrade = '''    [InitializeOnLoadMethod]
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

'''
assert marker in builder
builder = builder.replace(marker, upgrade + marker, 1)

for name in ['PlaygroundCamera.cs', 'GameManager.cs']:
    destination = project / 'Assets/Scripts' / name
    destination.write_text((stage / name).read_text(), newline='\r\n')
metadata = project / 'Assets/Scripts/PlaygroundCamera.cs.meta'
assert not metadata.exists()
metadata.write_text('fileFormatVersion: 2\nguid: 5c9786b4a6bc4d0ca66cf6ad0831ab27\n', newline='\r\n')
(project / 'Assets/Editor/BuildPlayground.cs').write_text(builder, newline='\r\n')
readme = (project / 'README.md').read_text()
readme = readme.replace('A small Unity 6 game for Week 2: Physics World. All graphics are primitive shapes.',
    'A small Unity 6 game for Week 2: Physics World. All graphics are primitive shapes.\n'
    'The fixed perspective camera, solid raised platform, and real-time shadows make height and landing positions visible.\n'
    'The camera fits the arena when the Game view changes size, and the HUD sits above the play area.')
readme = readme.replace('This replaces the current scene with the original level.',
    'This replaces the current scene with the original level.\n'
    'To update only the camera, lighting, and platform presentation of an existing level,\n'
    'use **Tools > Use 3D Playground View** outside Play mode. The original scene also upgrades automatically after script import.')
(project / 'README.md').write_text(readme, newline='\r\n')
print('Updated playground camera, HUD, scene upgrade, scene builder, and README.')
