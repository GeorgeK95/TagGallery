package TagGallery.bindingModel;

import javax.validation.constraints.NotNull;

/**
 * Created by George-Lenovo on 6/28/2017.
 */
public class CommentBindingModel {
    @NotNull
    private Long id;
    @NotNull
    private String content;
    @NotNull
    private Integer votes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
