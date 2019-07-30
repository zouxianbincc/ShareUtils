# ShareUtils
# 1.Add it in your root build.gradle at the end of repositories:
    allprojects {
		  repositories {
			...
		  	maven { url 'https://jitpack.io' }
	  	}
	   }
# 2.Add the dependency
    dependencies {
	        implementation 'com.github.zouxianbincc:ShareUtils:Tag'
	}
# 3.build.gradle 配置 在defaultConfig节点下增加你的qq id信息
     defaultConfig {
    	...
 	
     manifestPlaceholders = [
             //  替换成你的qq_id
             qq_id: "123456789"
     ]
     
   }
# 4.初始化
   // init
     ShareConfig config = ShareConfig.instance()
             .qqId(QQ_ID)
             .wxId(WX_ID)
             .weiboId(WEIBO_ID)
             // 下面两个，如果不需要登录功能，可不填写
             .weiboRedirectUrl(REDIRECT_URL)
             .wxSecret(WX_ID);
     ShareManager.init(config);
     
# 5.
    ShareUtil.shareImage(this, SharePlatform.QQ, "http://image.com", shareListener);
    ShareUtil.shareText(this, SharePlatform.WX, "分享文字", shareListener);
    ShareUtil.shareMedia(this, SharePlatform.QZONE, "title", "summary", "targetUrl", "thumb", shareListener);
    
# 6.  
     // LoginPlatform.WEIBO  微博登录   
        // LoginPlatform.WX     微信登录
        // LoginPlatform.QQ     QQ登录 
        final LoginListener listener = new LoginListener() {
                @Override
                public void loginSuccess(LoginResult result) {
                    //登录成功， 如果你选择了获取用户信息，可以通过
                }
            
                @Override
                public void loginFailure(Exception e) {
                    Log.i("TAG", "登录失败");
                }
    
                @Override
                public void loginCancel() {
                    Log.i("TAG", "登录取消");
                }
            };
        LoginUtil.login(MainActivity.this, LoginPlatform.WEIBO, mLoginListener, isFetchUserInfo);
