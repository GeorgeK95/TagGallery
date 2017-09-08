package TagGallery.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import TagGallery.bindingModel.TagBindingModel;
import TagGallery.bindingModel.UserBindingModel;
import TagGallery.entity.Tag;
import TagGallery.entity.User;
import TagGallery.repository.ImageRepository;
import TagGallery.repository.TagRepository;
import TagGallery.repository.UserRepository;
import TagGallery.utils.DeleteImage;

import java.util.List;

/**
 * Created by George-Lenovo on 6/29/2017.
 */
@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String listAlbums(Model model) {
        List<User> users = this.userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("view", "admin/listUsers");
        return "base-layout";
    }

    @GetMapping("/admin/tags")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String listTags(Model model) {
        List<Tag> topTags = getTags();
        model.addAttribute("tags", topTags);
        model.addAttribute("view", "admin/listTags");
        return "base-layout";
    }

    @PostMapping("/admin/searchUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String searchForUser(UserBindingModel userBindingModel) {
        User u = this.userRepository.findByEmail(userBindingModel.getEmail());
        if (u != null) {
            return "redirect:/profile/" + u.getId();
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/searchTag")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String searchForTag(TagBindingModel tagBindingModel) {
        Tag tag = this.tagRepository.findByTagName(tagBindingModel.getTags());
        if (tag != null) {
            return "redirect:/list/" + tag.getId();
        }

        return "redirect:/admin/tags";
    }

    @GetMapping("/admin/deleteTag/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteTag(@PathVariable Long id, Model model) {
        Tag t = this.tagRepository.findOne(id);
        model.addAttribute("tag", t);
        model.addAttribute("view", "tag/delete");
        return "base-layout";
    }

    @PostMapping("/admin/deleteTag/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteTagProcess(@PathVariable Long id) {
        Tag t = this.tagRepository.findOne(id);
        DeleteImage.deleteImagesFiles(t.getImages());
        this.tagRepository.delete(t);
        return "redirect:/admin/tags";
    }
    private List<Tag> getTags() {
        return this.tagRepository.findByIdAsc();
    }
}
