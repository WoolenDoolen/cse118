using UnityEngine;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour
{
    public int totalPickups = 5;
    int collected;
    string message = "Collect all five gold cubes. Use the green pad to reach the high one.";
    GUIStyle textStyle;
    GUIStyle titleStyle;
    PlaygroundCamera playgroundCamera;

    public void Collect()
    {
        collected++;
        if (collected == totalPickups)
            message = "You win! Press R to play again.";
    }

    public void SetMessage(string value)
    {
        if (collected < totalPickups)
            message = value;
    }

    void Update()
    {
        if (Input.GetKeyDown(KeyCode.R))
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex);
    }

    void OnGUI()
    {
        if (textStyle == null)
        {
            textStyle = new GUIStyle(GUI.skin.label) { fontSize = 14, wordWrap = true };
            titleStyle = new GUIStyle(textStyle) { fontSize = 18, fontStyle = FontStyle.Bold };
        }

        if (playgroundCamera == null && Camera.main != null)
            playgroundCamera = Camera.main.GetComponent<PlaygroundCamera>();
        bool hardMode = playgroundCamera != null && playgroundCamera.HardMode;
        float width = Mathf.Max(1f, Screen.width - 24f);
        GUI.Box(new Rect(0f, 0f, Screen.width, PlaygroundCamera.HudHeight), "");
        GUI.Label(new Rect(12f, 6f, width, 24f),
            (hardMode ? "HARD MODE    Gold cubes: " : "PHYSICS PLAYGROUND    Gold cubes: ")
            + collected + " / " + totalPickups, titleStyle);
        GUI.Label(new Rect(12f, 31f, width, 32f),
            "WASD: move    Space: jump    E: spawn    R: restart    Arrows: camera    L: reset    H: hard mode", textStyle);
        GUI.Label(new Rect(12f, 66f, width, 32f), message, textStyle);
    }
}
