package TagGallery.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by George-Lenovo on 6/28/2017.
 */

@Entity
@Table(name = "comment")
public class Comment {
    private Long id;

    private User author;

    private String content;

    private Integer votes;

    private Image imageHolder;

    private Date postDate;

    private Set<User> likers;

    public Comment(User author, String content, Integer votes, Image image) {
        this.author = author;
        this.content = content;
        this.votes = votes;
        this.postDate = new Date();
        this.imageHolder = image;
        this.likers = new HashSet<>();
    }

    public Comment() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToMany/*fetch = FetchType.EAGER*/
    @JoinTable(name = "comments_users")
    public Set<User> getLikers() {
        return likers;
    }

    public void setLikers(Set<User> likers) {
        this.likers = likers;
    }

    @NotNull
    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date date) {
        this.postDate = date;
    }

    @OneToOne()
    @NotNull
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Column(columnDefinition = "text", nullable = false, length = 400)
    @NotNull
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(columnDefinition = "int default 0")
    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    @ManyToOne(/*cascade = CascadeType.REMOVE*/)
    @NotNull
    @JoinColumn(nullable = false, name = "imageId")
    public Image getImageHolder() {
        return imageHolder;
    }

    public void setImageHolder(Image imageHolderId) {
        this.imageHolder = imageHolderId;
    }

    public void addUserLike(User user) {
        this.likers.add(user);
    }

    public String shortContent() {
        return this.content.length() > 100 ? this.content.substring(0, 100).concat("...") : this.content;
    }
}
