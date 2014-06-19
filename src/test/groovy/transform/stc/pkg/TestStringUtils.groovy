package groovy.transform.stc.pkg

class TestStringUtils {
    static String repeat(String self, int times) {
        return self * times;
    }
    
    static String capitalizeInUtils(String self) {
        return self.capitalize();
    }
}
