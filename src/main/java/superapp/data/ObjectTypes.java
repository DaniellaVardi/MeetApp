package superapp.data;

public enum ObjectTypes {
    Group,
    Transaction,
    GrabPoll,
    Drive;

    public static boolean isValidObjectType(String objectType) {
        if (objectType == null)
            return false;
        try {
            ObjectTypes.valueOf(objectType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
