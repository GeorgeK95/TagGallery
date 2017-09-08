package TagGallery.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import TagGallery.bindingModel.UserBindingModel;
import TagGallery.bindingModel.UserInfoBindingModel;
import TagGallery.entity.Album;
import TagGallery.entity.Role;
import TagGallery.entity.User;
import TagGallery.repository.AlbumRepository;
import TagGallery.repository.RoleRepository;
import TagGallery.repository.UserRepository;
import TagGallery.utils.DeleteImage;
import TagGallery.utils.Paths;
import TagGallery.utils.UploadImage;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AlbumRepository albumRepository;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("view", "user/register");
        return "base-layout";
    }

    @PostMapping("/register")
    public String registerProcess(UserBindingModel userBindingModel) {
        if (this.userRepository.getAllEmails().contains(userBindingModel.getEmail())) {
            return "redirect:/register";
        }
        if (!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())) {
            return "redirect:/register";
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        User user = new User(
                userBindingModel.getEmail(),
                userBindingModel.getFullName(),
                bCryptPasswordEncoder.encode(userBindingModel.getPassword()));

        createRoles();
        Role userRole = this.roleRepository.findByName("ROLE_USER");
        user.addRole(userRole);
        // add role admin if the user is the first registered
        if (this.userRepository.findOne((long) 1) == null) {
            Role adminRole = this.roleRepository.findByName("ROLE_ADMIN");
            user.addRole(adminRole);
        }
        setImageOnRegistration(user);
        this.userRepository.saveAndFlush(user);
        createDefaultAlbum(user);
        return "redirect:/login";
    }

    private void createRoles() {
        if (this.roleRepository.findByName("ROLE_USER") == null && this.roleRepository.findByName("ROLE_ADMIN") == null) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            Role userAdmin = new Role();
            userAdmin.setName("ROLE_ADMIN");
            this.roleRepository.saveAndFlush(userRole);
            this.roleRepository.saveAndFlush(userAdmin);
        }
    }

    private void createDefaultAlbum(User user) {
        Album album = new Album();
        album.setAlbumProfileImage(Paths.DEFAULT_ALBUM_IMAGE_PATH);
        album.setAuthor(user);
        album.setTitle("Default Album");
        this.albumRepository.saveAndFlush(album);
    }

    private void setImageOnRegistration(User user) {
        user.setProfilePicture(Paths.PROFILE_IMAGE_PATH);
        user.setProfilePictureSmall(Paths.PROFILE_SMALL_IMAGE_PATH);
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("view", "user/login");
        return "base-layout";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = getUser();

        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }

    @GetMapping("/profile/{id}")
    public String profilePage(@PathVariable Long id, Model model) {
        User user = this.userRepository.findOne(id);

        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }

    private boolean hasRights(User browsingUser) {
        User currentLoggedInUser = getUser();
        if (browsingUser != null && (browsingUser.getEmail().equals(currentLoggedInUser.getEmail()) || currentLoggedInUser.getRoles().stream()
                .map(Role::getName).collect(Collectors.toList()).contains("ROLE_ADMIN"))) {
            return true;
        }

        return false;
    }

    @GetMapping("/profile/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProfile(@PathVariable Long id, Model model) {
        if (!this.userRepository.exists(id)) {
            return "redirect:/";
        }

        User user = this.userRepository.findOne(id);
        if (hasRights(user)) {
            model.addAttribute("user", user);
            model.addAttribute("view", "user/editProfile");
            return "base-layout";
        }
        return "redirect:/profile/" + id;
    }


    @PostMapping("/profile/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProfileProcess(@PathVariable Long id,
                                     UserBindingModel userBindingModel) {
        if (!this.userRepository.exists(id)) {
            return "redirect:/";
        }

        User user = this.userRepository.findOne(id);
        if (!hasRights(user)) {
            return "redirect:/";
        }

        if (!StringUtils.isEmpty(userBindingModel.getPassword())
                && !StringUtils.isEmpty(userBindingModel.getConfirmPassword())) {

            if (userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())) {
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

                user.setPassword(bCryptPasswordEncoder.encode(userBindingModel.getPassword()));
            }
        }

        user.setFullName(userBindingModel.getFullName());

        this.userRepository.saveAndFlush(user);

        if (getUser().getRoles().stream().map(Role::getName).collect(Collectors.toList()).contains("ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }
        return "redirect:/profile";
    }

    @GetMapping("/profile/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editInfo(@PathVariable Long id, Model model) {
        if (!this.userRepository.exists(id)) {
            return "redirect:/";
        }

        User user = this.userRepository.findOne(id);
        if (hasRights(user)) {
            model.addAttribute("user", user);
            model.addAttribute("view", "user/info");

            return "base-layout";
        }
        return "redirect:/profile/" + id;

    }

    @GetMapping("/admin/deleteUser/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteUser(@PathVariable Long id, Model model) {
        if (!this.userRepository.exists(id)) {
            return "redirect:/profile";
        }

        User user = this.userRepository.findOne(id);

        model.addAttribute("user", user);
        model.addAttribute("view", "user/delete");

        return "base-layout";
    }

    @PostMapping("/admin/deleteUser/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteUserProccess(@PathVariable Long id) {
        if (!this.userRepository.exists(id)) {
            return "redirect:/admin/users";
        }

        User user = this.userRepository.findOne(id);
        if (user != null && !user.getEmail().equals(getUser().getEmail()) && user.getId() != 1) {
            deleteUserAlbums(user.getAlbums());
            deleteProfilePictures(user);
            this.userRepository.delete(user);
        }

        return "redirect:/admin/users";
    }

    private void deleteProfilePictures(User user) {
        List<String> oldImagePaths = getUserProfilePicturePaths(user);
        UploadImage.deleteOriginalFiles(oldImagePaths);
    }

    private void deleteUserAlbums(List<Album> albums) {
        for (Album album : albums) {
            DeleteImage.deleteAlbumFiles(album);
        }
    }

    @PostMapping("/profile/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editInfoProcess(@PathVariable Long id, UserInfoBindingModel userInfoBindingModel) {
        if (!this.userRepository.exists(id)) {
            return "redirect:/profile";
        }

        User user = this.userRepository.findOne(id);
        if (!hasRights(user)) {
            return "redirect:/profile";
        }

        user.setTown(userInfoBindingModel.getTown());
        user.setCountry(userInfoBindingModel.getCountry());
        user.setPhone(userInfoBindingModel.getTelephoneNumber());
        user.setInformation(userInfoBindingModel.getInformation());
        String admin = userInfoBindingModel.getAdmin();
        if (admin != null) {
            if (admin.equals("yes")) {
                user.addRole(this.roleRepository.findByName("ROLE_ADMIN"));
            } else if (admin.equals("no")) {
                user.removeAdminRole();
            }
        }
        if (!userInfoBindingModel.getProfilePicture().getOriginalFilename().equals("")) {
            uploadImage(userInfoBindingModel, user);
        }

        this.userRepository.saveAndFlush(user);
        if (getUser().getRoles().stream().map(Role::getName).collect(Collectors.toList()).contains("ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }
        return "redirect:/profile";
    }

    private User getUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return this.userRepository.findByEmail(principal.getUsername());
    }

    private void uploadImage(UserInfoBindingModel userInfoBindingModel, User user) {
        String imageName = user.getProfilePicture().substring(16);
        List<String> oldImagePaths = getUserProfilePicturePaths(user);
        if (!imageName.equals("user_default_image.png")) {
            UploadImage.deleteOriginalFiles(oldImagePaths);
        }
        List<String> imagePaths = UploadImage.uploadProfileImages(userInfoBindingModel.getProfilePicture(), imageName);
        user.setProfilePicture(imagePaths.get(0));
        user.setProfilePictureSmall(imagePaths.get(1));
    }

    private List<String> getUserProfilePicturePaths(User user) {
        List<String> oldImagePaths = new ArrayList<String>();
        String profilePicture = user.getProfilePicture();
        String profilePictureSmall = user.getProfilePictureSmall();
        if (!profilePicture.substring(16).equals(Paths.PROFILE_DEFAULT_IMAGE_NAME) &&
                !profilePictureSmall.substring(16).equals(Paths.PROFILE_DEFAULT_SMALL_IMAGE_NAME)) {
            oldImagePaths.add(Paths.PROFILE_IMAGE_DIRECTORY + "\\" + profilePicture.substring(16));
            oldImagePaths.add(Paths.PROFILE_IMAGE_DIRECTORY + "\\" + profilePictureSmall.substring(16));
        }

        return oldImagePaths;
    }


}

