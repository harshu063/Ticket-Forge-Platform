package com.example.ticket.filters;

@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        // TODO Implementation for user provisioning filter

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null 
        && authentication.isAuthenticated() 
        && authentication.getPrincipal() instanceof Jwt jwt) {
            UUID keycloakId = UUID.fromString(jwt.getSubject());
            
            // Check if the user exists in the database, if not create a new user
            if (!userRepository.existsById(keycloakId)) {
                User user = new User();
                user.setId(keycloakId);
                user.setName(jwt.getClaimAsString("preferred_username"));
                user.setEmail(jwt.getClaimAsString("email"));
                // Set other user properties as needed
                
                userRepository.save(user);
            }
        }
        filterChain.doFilter(request, response);
    }
}
