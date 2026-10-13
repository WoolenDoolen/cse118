using UnityEngine;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour
{
    public int totalPickups = 5;
    int collected;
    string message = "Collect all five gold cubes. Use the green pad to reach the high one.";
    GUIStyle textStyle;

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
            textStyle = new GUIStyle(GUI.skin.label) { fontSize = 18, wordWrap = true };

        GUI.Box(new Rect(10, 10, Mathf.Min(Screen.width - 20, 720), 130), "");
        GUI.Label(new Rect(24, 18, Screen.width - 48, 26),
            "PHYSICS PLAYGROUND    Gold cubes: " + collected + " / " + totalPickups, textStyle);
        GUI.Label(new Rect(24, 48, Screen.width - 48, 26),
            "WASD / arrows: move    Space: jump    E: spawn at blue button    R: restart", textStyle);
        GUI.Label(new Rect(24, 82, Mathf.Min(Screen.width - 48, 680), 52), message, textStyle);
    }
}
