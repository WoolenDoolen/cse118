using UnityEngine;

public class Pickup : MonoBehaviour
{
    bool collected;

    void Update()
    {
        transform.Rotate(0, 60f * Time.deltaTime, 0, Space.World);
    }

    void OnTriggerEnter(Collider other)
    {
        if (collected || !other.TryGetComponent<PlayerController>(out _))
            return;

        collected = true;
        FindAnyObjectByType<GameManager>().Collect();
        Destroy(gameObject);
    }
}
