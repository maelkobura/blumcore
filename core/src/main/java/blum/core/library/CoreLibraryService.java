package blum.core.library;

import blum.api.exception.ServiceStartException;
import blum.api.library.LibraryService;
import blum.api.services.Service;
import blum.api.services.annotation.ServiceDescriptor;
import blum.core.system.BlumBoot;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ServiceDescriptor(
        name = "library",
        displayName = "Blum Game Library",
        version = "1.0.0",
        description = "The Blum Game Database and Launch Service"
)
@Slf4j
public class CoreLibraryService implements Service, LibraryService {

    private File gameDatabaseFile;
    private Map<String, DatabaseHandler> handlerMap;

    @Override
    public void start() throws ServiceStartException {
        BlumBoot.DATA_DIR.mkdir();
        this.gameDatabaseFile = new File(BlumBoot.DATA_DIR, "games.db");
        this.handlerMap = new HashMap<>();

        this.handlerMap.put("game-metadata", DatabaseHandler.create(gameDatabaseFile, "game-metadata"));

    }

    @Override
    public void stop() throws ServiceStartException {
        for(Map.Entry<String, DatabaseHandler> hand : handlerMap.entrySet()) {
            try {
                hand.getValue().close();
            } catch (SQLException e) {
                log.error("Failed to close database ({})", hand.getKey(), e);
            }
        }
    }
}
