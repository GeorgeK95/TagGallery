package TagGallery.entity;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import TagGallery.utils.Paths;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Set<Role> roles;

    private String town;
    private String country;
    private String information;
    private String profilePicture = System.getProperty("user.dir") + Paths.PROFILE_IMAGE_DIRECTORY + Paths.PROFILE_FOLDER_PATH;
    private String profilePictureSmall;
    private String phone;
    private List<Album> albums;

    private Set<Comment> commentsLiked;

    public User(String email, String fullName, String password) {
        this(email, fullName, password, "missing");
    }

    public User(String email, String fullName, String password, String missingString) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.roles = new HashSet<>();
        this.town = missingString;
        this.country = missingString;
        this.information = missingString;
        this.phone = missingString;
        this.albums = new ArrayList<>();
    }

    @ManyToMany(mappedBy = "likers", cascade = CascadeType.REMOVE)
    public Set<Comment> getCommentsLiked() {
        return commentsLiked;
    }

    public void setCommentsLiked(Set<Comment> commentsLiked) {
        this.commentsLiked = commentsLiked;
    }

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "email", unique = true, nullable = false, length = 30)
    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "fullName", length = 30)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "password", length = 60, nullable = false)
    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles")
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeAdminRole() {
        List<Role> rolesList = this.roles.stream().collect(Collectors.toList());
        for (int i = 0; i < rolesList.size(); i++) {
            Role current = rolesList.get(i);
            if (current.getName().equals("ROLE_ADMIN")) {
                rolesList.remove(i);
            }
        }
        this.roles = rolesList.stream().collect(Collectors.toSet());
    }

    @Column(name = "town", length = 30)
    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    @Column(name = "country", length = 30)
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Column(columnDefinition = "text", name = "information")
    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    @Column(name = "profile_image")
    public String getProfilePicture() {
        return profilePicture;
    }

    @Column(name = "profile_image_small")
    public String getProfilePictureSmall() {
        return profilePictureSmall;
    }

    public void setProfilePictureSmall(String profilePictureSmall) {
        this.profilePictureSmall = profilePictureSmall;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Column(name = "phone", length = 30)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Transient
    public boolean isAdmin() {
        List<String> userDetails = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return userDetails.contains("ROLE_ADMIN");
    }

    @Transient
    public boolean isAuthor(Album album) {
        String username = getCurrentUser();
        return Objects.equals(username, album.getAuthor().getEmail());
    }

    @Transient
    public boolean isCommentAuthor(Comment comment) {
        String username = getCurrentUser();
        String email = comment.getAuthor().getEmail();
        return username.equals(email);
    }

    public boolean isImageAuthor(Image image) {
        String username = getCurrentUser();
        String email = image.getAlbumHolder().getAuthor().getEmail();
        return username.equals(email);
    }

    public void addAlbum(Album album) {
        this.albums.add(album);
    }

    @Transient
    public boolean isLoggedUserAuthorOfAlbum(User user) {
        String currentUser = getCurrentUser();
        return Objects.equals(currentUser, user.getEmail());
    }

    @Transient
    private String getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }
}