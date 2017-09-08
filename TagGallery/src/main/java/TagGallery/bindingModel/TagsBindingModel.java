package TagGallery.bindingModel;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

public class TagsBindingModel {
    @NotNull
    private String[] tags;

    public String[] getTags() {
        return (String[]) Arrays.stream(tags).map(String::toLowerCase).toArray();
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}