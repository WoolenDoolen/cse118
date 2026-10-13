using UnityEngine;

public class CrateLifetime : MonoBehaviour
{
    void Start()
    {
        Destroy(gameObject, 12f);
    }
}
