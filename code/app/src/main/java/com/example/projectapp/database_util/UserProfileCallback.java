// -----------------------------------------------------------------------------
// File: UserProfileCallback.java
// -----------------------------------------------------------------------------
// This file provides an interface that collaborates with FirebaseSync to as a
// callback for classes to implement behavior for when they receive the data
//
// Design Pattern: Data Access Object
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.database_util;

import com.example.projectapp.models.UserProfile;

public interface UserProfileCallback {
    void onUserProfileLoaded(UserProfile userProfile);
    void onFailure(Exception e);
}