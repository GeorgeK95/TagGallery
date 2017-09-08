package TagGallery.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "image")
public class Image {

    private Long id;
    private Double rating;

    private String imagePath;
    private String bigImagePath;
    private String smallImagePath;
    private Album albumHolder;

    private Date postDate;
    private Set<Comment> comments;
    private Set<Tag> tags;

    public Image() {
    }

    public Image(String path, Album album) {
        this.rating = 0d;
        this.albumHolder = album;
        this.imagePath = path;
        this.tags = new HashSet<>();
        this.comments = new HashSet<>();
        this.postDate = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(columnDefinition = "double default 0")
    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @ManyToOne
    @NotNull
    @JoinColumn(nullable = false, name = "albumId")
    public Album getAlbumHolder() {
        return albumHolder;
    }

    public void setAlbumHolder(Album albumHolder) {
        this.albumHolder = albumHolder;
    }


    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "images_tags", joinColumns = {
            @JoinColumn(name = "image_id")}, inverseJoinColumns = {
            @JoinColumn(name = "tag_id")})
    @NotNull
    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public String getBigImagePath() {
        return bigImagePath;
    }

    public void setBigImagePath(String bigImagePath) {
        this.bigImagePath = bigImagePath;
    }

    public String getSmallImagePath() {
        return smallImagePath;
    }

    public void setSmallImagePath(String smallImagePath) {
        this.smallImagePath = smallImagePath;
    }

    @OneToMany(mappedBy = "imageHolder", cascade = CascadeType.REMOVE)
    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    @NotNull
    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    @Transient
    public String getName() {
        return this.imagePath.substring(14);
    }
}
