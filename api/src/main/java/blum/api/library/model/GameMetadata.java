package blum.api.library.model;

import blum.api.annotation.Named;
import blum.api.database.annotation.Model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Model
public class GameMetadata {

    private int id;

    private String name;

    @Named("sort_name")
    private String sortName;

    private String platform;
    private String genres;
    private String developers;
    private String publishers;
    private String categories;
    private String features;
    private String tags;
    private String description;

    @Named("release_date")
    private String releaseDate;

    private String series;
    private String pegi;
    private String region;
    private String source;

    @Named("press_rating")
    private Integer pressRating;

    @Named("community_rating")
    private Integer communityRating;
}
