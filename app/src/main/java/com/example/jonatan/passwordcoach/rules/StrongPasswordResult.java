package com.example.jonatan.passwordcoach.rules;

public class StrongPasswordResult implements Result {
    @Override
    public boolean passwordIsStrong() {
        return true;
    }
}