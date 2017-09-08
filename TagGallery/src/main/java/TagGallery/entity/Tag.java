package TagGallery.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(unique = true)
    private String tagName;
    @ManyToMany(mappedBy = "tags", cascade = CascadeType.REMOVE)
    private Set<Image> images;

    @Transient
    private Image randomImage;

    public Image getRandomImage() {
        return randomImage;
    }

    public void setRandomImage(Image randomImage) {
        this.randomImage = randomImage;
    }

    public Tag() {
    }

    public Tag(String tagName) {
        this.tagName = tagName;
        this.images = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public void addImage(Image image) {
        this.images.add(image);
    }

    public String displayTagName() {
        return "#" + this.tagName;
    }
}
