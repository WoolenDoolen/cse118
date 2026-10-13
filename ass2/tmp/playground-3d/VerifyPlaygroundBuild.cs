using System.IO;
using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEditor.SceneManagement;
using UnityEngine;

public static class VerifyPlaygroundBuild
{
    public static void Run()
    {
        const string scene = "Assets/Scenes/PhysicsPlayground.unity";
        EditorSceneManager.OpenScene(scene);
        BuildPlayground.Use3DView();
        AssetDatabase.SaveAssets();
        string folder = Path.GetFullPath("../verification-player");
        Directory.CreateDirectory(folder);
        var options = new BuildPlayerOptions
        {
            scenes = new[] { scene },
            locationPathName = Path.Combine(folder, "Playground.exe"),
            target = BuildTarget.StandaloneWindows64,
            options = BuildOptions.Development
        };
        BuildReport report = BuildPipeline.BuildPlayer(options);
        if (report.summary.result != BuildResult.Succeeded)
            throw new System.Exception("Verification player build failed: " + report.summary.result);
        Debug.Log("VERIFY BUILD PASSED");
    }
}
