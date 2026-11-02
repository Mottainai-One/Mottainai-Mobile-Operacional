package com.mottainai.operacional.notifications;
import android.content.Intent;
import com.mottainai.operacional.utils.RoleHelper;
import java.util.Locale;
import java.util.Map;

/** Authorized navigation for the real internal notification inbox. No FCM or system push. */
public final class NotificationRouter {
    private NotificationRouter(){}
    public static final String EXTRA_DESTINATION="notification_destination";
    public static final String EXTRA_ENTITY_ID="notification_entity_id";
    public static final String EXTRA_NOTIFICATION_ID="notification_id";
    public enum Destination {
        HOME,PRODUCT,DAMAGE,ALERT,SUGGESTION,INVENTORY,POS_APPROVAL;
        public static Destination fromPayload(Map<String,String> data){
            String value=data.get("notificationType");if(value==null)value=data.get("type");if(value==null)value=data.get("route");
            if(value==null)return HOME;
            switch(value.trim().toLowerCase(Locale.ROOT)){
                case "product":case "products":return PRODUCT;
                case "damage":case "damages":case "avaria":return DAMAGE;
                case "alert":case "alerts":case "ia/alerts":return ALERT;
                case "suggestion":case "suggestions":case "ia/suggestions":return SUGGESTION;
                case "inventory":case "inventario":return INVENTORY;
                case "pos_approval":case "pos_approvals":return POS_APPROVAL;
                default:return HOME;
            }
        }
    }
    public static Destination destinationFromIntent(Intent intent){
        if(intent==null)return null;
        String value=intent.getStringExtra(EXTRA_DESTINATION);if(value==null)return null;
        try{return Destination.valueOf(value);}catch(IllegalArgumentException error){return null;}
    }
    public static boolean isDestinationAllowed(Destination destination,String role){
        if(destination==null||!RoleHelper.isKnownRole(role))return false;
        if(destination==Destination.SUGGESTION)return RoleHelper.canViewSuggestions(role);
        if(destination==Destination.POS_APPROVAL)return RoleHelper.canApprovePosRequests(role);
        return true;
    }
}
