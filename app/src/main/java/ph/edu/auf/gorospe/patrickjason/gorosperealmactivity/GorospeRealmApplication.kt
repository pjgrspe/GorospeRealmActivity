package ph.edu.auf.gorospe.patrickjason.gorosperealmactivity

import android.app.Application
import ph.edu.auf.gorospe.patrickjason.gorosperealmactivity.data.database.RealmHelper

class GorospeRealmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RealmHelper.initializeRealm()
    }

    override fun onTerminate() {
        super.onTerminate()
        RealmHelper.closeRealm()
    }
}