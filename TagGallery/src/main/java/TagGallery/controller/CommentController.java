package TagGallery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import TagGallery.bindingModel.CommentBindingModel;
import TagGallery.entity.Comment;
import TagGallery.entity.Image;
import TagGallery.entity.User;
import TagGallery.repository.CommentRepository;
import TagGallery.repository.ImageRepository;
import TagGallery.repository.UserRepository;

import java.util.Date;
import java.util.Set;

@Controller
public class CommentController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/postComment/{id}")
    @PreAuthorize("isAuthenticated()")
    public String comment(@PathVariable Long id, Model model) {
        Image img = this.imageRepository.findOne(id);
        model.addAttribute("view", "comment/addComment");
        model.addAttribute("image", img);
        return "base-layout";
    }

    @PostMapping("/postComment/{id}")
    @PreAuthorize("isAuthenticated()")
    public String commentProcess(@PathVariable Long id, CommentBindingModel commentBindingModel) {
        User author = getUser();
        Image image = this.imageRepository.findOne(id);
        Comment comment = new Comment(author, commentBindingModel.getContent(), 0, image);
        this.commentRepository.saveAndFlush(comment);
        return "redirect:/image/" + id;
    }

    private User getUser() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return this.userRepository.findByEmail(user.getUsername());
    }

    @GetMapping("/image/{picId}/likeComment/{comId}")
    @PreAuthorize("isAuthenticated()")
    public String like(@PathVariable Long picId, @PathVariable Long comId) {
        User us = getUser();
        Comment comment = this.commentRepository.findOne(comId);

        boolean userHasLikedComment = checkIfUserHasLikedComment(us, comment);
        if (!userHasLikedComment) {
            comment.addUserLike(us);
            comment.setVotes(comment.getVotes() + 1);
            this.commentRepository.saveAndFlush(comment);
        }

        return "redirect:/image/" + picId;
    }

    @GetMapping("/image/{picId}/page/{pageNum}")
    public String page(@PathVariable Long picId, @PathVariable Long pageNum) {
        if (pageNum < 1) {
            pageNum = (long) 1;
        }

        Image image = this.imageRepository.findOne(picId);
        Long totalPages = (long) (Math.ceil(this.commentRepository.findByImageHolderOrderByIdAsc(image).size() / 5.0));
        if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        ImageController.currentPage = pageNum;
        return "redirect:/image/" + picId;
    }

    private boolean checkIfUserHasLikedComment(User us, Comment comment) {
        Set<User> likers = comment.getLikers();
        for (User liker : likers) {
            if (liker.getEmail().equals(us.getEmail())) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/comment/options/{id}")
    public String options(@PathVariable Long id, Model model) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/albums";
        }

        Comment comment = this.commentRepository.findOne(id);

        model.addAttribute("view", "comment/options");
        model.addAttribute("comment", comment);
        model.addAttribute("user", comment.getAuthor());
        return "base-layout";
    }

    @GetMapping("/comment/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Long id, Model model) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/albums";
        }

        Comment comment = this.commentRepository.findOne(id);

        if (!isUserAuthor(comment)) {
            return "redirect:/image/" + comment.getImageHolder().getId();
        }

        model.addAttribute("view", "comment/edit");
        model.addAttribute("comment", comment);
        return "base-layout";
    }

    @PostMapping("/comment/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Long id, CommentBindingModel commentBindingModel) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if (isUserAuthor(comment)) {
            comment.setContent(commentBindingModel.getContent());
            comment.setPostDate(new Date());
            this.commentRepository.saveAndFlush(comment);
        }

        return "redirect:/image/" + comment.getImageHolder().getId();

    }

    @GetMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Long id, Model model) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/albums";
        }

        Comment comment = this.commentRepository.findOne(id);
        Image img = comment.getImageHolder();

        if (isUserAuthorOrAdmin(comment) || isUserAuthor(img)) {
            model.addAttribute("view", "comment/delete");
            model.addAttribute("comment", comment);
            return "base-layout";

        }
        return "redirect:/image/" + comment.getImageHolder().getId();
    }

    private boolean isUserAuthor(Image img) {
        return getUser().getEmail().equals(img.getAlbumHolder().getAuthor().getEmail());
    }

    @PostMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Long id) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/albums";
        }

        Comment comment = this.commentRepository.findOne(id);
        Image img = comment.getImageHolder();

        if (isUserAuthorOrAdmin(comment) || isUserAuthor(img)) {
            this.commentRepository.delete(comment);
        }

        return "redirect:/image/" + comment.getImageHolder().getId();
    }

    private boolean isUserAuthorOrAdmin(Comment comment) {
        UserDetails user = getUserDetails();
        User userEntity = this.userRepository.findByEmail(user.getUsername());
        return userEntity.isAdmin() || userEntity.isCommentAuthor(comment);
    }

    private UserDetails getUserDetails() {
        return (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private boolean isUserAuthor(Comment comment) {
        UserDetails user = getUserDetails();
        User userEntity = this.userRepository.findByEmail(user.getUsername());
        return userEntity.isCommentAuthor(comment);
    }


}
