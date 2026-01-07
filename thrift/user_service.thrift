namespace java UserService
namespace py UserService
namespace rb UserService

struct User {
    1: i64 id,
    2: string name,
    3: i32 age
}

exception UserNotFoundException {
    1: string message
}

service UserService {
    // Create a new user
    User createUser(1: string name, 2: i32 age),
    
    // Get user by ID
    User getUser(1: i64 userId) throws (1: UserNotFoundException ex),
    
    // Update user information
    User updateUser(1: User user) throws (1: UserNotFoundException ex),
    
    // Delete user
    void deleteUser(1: i64 userId) throws (1: UserNotFoundException ex),
    
    // List all users
    list<User> getAllUsers()
}