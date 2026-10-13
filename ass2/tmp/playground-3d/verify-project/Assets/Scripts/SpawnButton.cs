using UnityEngine;

public class SpawnButton : MonoBehaviour
{
    public GameObject cratePrefab;
    public Transform spawnPoint;
    bool playerNearby;
    float nextSpawnTime;

    void OnTriggerEnter(Collider other)
    {
        if (!other.TryGetComponent<PlayerController>(out _))
            return;
        playerNearby = true;
        FindAnyObjectByType<GameManager>().SetMessage("Press E to spawn a physics crate. Push it around; it disappears after 12 seconds.");
    }

    void OnTriggerExit(Collider other)
    {
        if (other.TryGetComponent<PlayerController>(out _))
            playerNearby = false;
    }

    void Update()
    {
        if (playerNearby && Input.GetKeyDown(KeyCode.E) && Time.time >= nextSpawnTime)
        {
            Instantiate(cratePrefab, spawnPoint.position, Quaternion.identity);
            nextSpawnTime = Time.time + 0.5f;
        }
    }
}
