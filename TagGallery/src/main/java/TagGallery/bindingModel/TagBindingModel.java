package TagGallery.bindingModel;

import javax.validation.constraints.NotNull;

public class TagBindingModel {
    @NotNull
    private String tags;

    public String getTags() {
        return tags != null ? tags.toLowerCase() : null;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}