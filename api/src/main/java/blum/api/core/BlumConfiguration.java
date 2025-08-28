package blum.api.core;

import blum.api.configuration.ConfigurationRoot;
import blum.api.annotation.Named;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlumConfiguration extends ConfigurationRoot {
    public BlumConfiguration(String file) {super(file);}

    @Named("global.debug")
    private boolean debugMode = false;

    @Named("network.port")
    private int networkPort = 1256;

    @Named("network.security.allowConnectionOutsideLocalComputer")
    private boolean allowConnectionOutsideLocalComputer = false;

    @Named("network.launch.allowLaunchFromNetwork")
    private boolean allowLaunchFromNetwork = true;

    @Named("ui.systemDialog")
    private boolean allowSystemDialog = true;

}
