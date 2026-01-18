package sodyl.proyecto.networking;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;

public class SharedResourceManager {
    private Map<String, Long> depletedResources = new HashMap<>();
    private static final long DEFAULT_COOLDOWN = 1000 * 60;

    public void markResourceDepleted(String tileId, long durationMs) {
        depletedResources.put(tileId, System.currentTimeMillis() + durationMs);
        Gdx.app.log("RESOURCE", "Resource depleted at " + tileId + " for " + durationMs + "ms");
    }

    public void markResourceDepleted(String tileId) {
        markResourceDepleted(tileId, DEFAULT_COOLDOWN);
    }

    public boolean isResourceAvailable(String tileId) {
        if (!depletedResources.containsKey(tileId))
            return true;

        long respawnTime = depletedResources.get(tileId);
        if (System.currentTimeMillis() >= respawnTime) {
            depletedResources.remove(tileId);
            return true;
        }
        return false;
    }
}
