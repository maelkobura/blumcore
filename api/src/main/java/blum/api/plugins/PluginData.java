package blum.api.plugins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class PluginData {

    private String name;
    private String version;
    private String description;
    private String author;
    private String website;

    private List<String> associatedServices = new ArrayList<>();
    private List<String> associatedEndpoints = new ArrayList<>();
    private List<String> associatedIntegrations = new ArrayList<>();

    @Override
    public Object clone() {
        return new PluginData(name, version, description, author, website, associatedServices.stream().toList(), associatedEndpoints.stream().toList(), associatedIntegrations.stream().toList());
    }
}
