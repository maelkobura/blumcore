package blum.core.library.repositories;

import blum.api.database.BlumRepository;
import blum.api.library.model.GameMetadata;
import blum.core.util.FileUtil;

import java.io.InvalidClassException;
import java.sql.Connection;

public class GameMetadataRepository extends BlumRepository<GameMetadata> {

    public GameMetadataRepository(Connection connection, Class<GameMetadata> type) throws InvalidClassException {
        super(connection, type);
    }

    @Override
    public String getTableName() {
        return "metadata";
    }

    @Override
    public String initTable() throws Exception {
        return FileUtil.getFileContentFromJar("database/game-metadata.sql");
    }

    public GameMetadata getGame(String name) {
        return query("SELECT * FROM metadata WHERE name = ?", name);
    }

}
