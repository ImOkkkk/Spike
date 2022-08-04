package com.liuwy.context;


/**
 * @author ImOkkkk
 * @date 2022/6/13 10:20
 * @since 1.0
 */
public class WebContext {

  private static ThreadLocal<String> currentUser = new ThreadLocal<String>();


  /**
   * 已过期
   */
  private static ThreadLocal<Boolean> isExpired = ThreadLocal.withInitial(() -> false);


  private WebContext() {
    throw new UnsupportedOperationException();
  }

  public static String getCurrentUser() {
    return currentUser.get();
  }

  public static void setCurrentUser(String userId) {
    currentUser.set(userId);
  }

  public static void removeCurrentUser() {
    currentUser.remove();
  }

  public static Boolean getIsExpired() {
    return isExpired.get();
  }

  public static void setIsExpired(Boolean expired) {
    isExpired.set(expired);
  }

  public static void removeIsExpired() {
    isExpired.remove();
  }

}