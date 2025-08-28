package blum.core.library;

import blum.api.core.Blum;
import blum.api.database.DatabaseService;
import blum.api.exception.ServiceStartException;
import blum.api.library.LibraryService;
import blum.api.library.model.GameMetadata;
import blum.api.services.Service;
import blum.api.services.annotation.ServiceDescriptor;
import blum.core.library.repositories.GameMetadataRepository;
import lombok.extern.slf4j.Slf4j;

@ServiceDescriptor(
        name = "library",
        displayName = "Blum Game Library",
        version = "1.0.0",
        description = "The Blum Game Database and Launch Service"
)
@Slf4j
public class CoreLibraryService implements Service, LibraryService {

    private GameMetadataRepository gameMetadataRepository;

    @Override
    public void start() throws ServiceStartException {
        try {
            gameMetadataRepository = Blum.getServiceManager().getService("database", DatabaseService.class).createOrGet("game", GameMetadata.class, GameMetadataRepository.class);
        } catch (Exception e) {
            throw new ServiceStartException("Failed to initialize libraries database", e);
        }

    }

    @Override
    public void stop() throws ServiceStartException {

    }
}
