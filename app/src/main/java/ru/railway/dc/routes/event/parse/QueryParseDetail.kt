package ru.railway.dc.routes.event.parse

import ru.railway.dc.routes.event.parse.method.DBParseDetail
import ru.railway.dc.routes.event.parse.method.HTMLParseDetail
import ru.railway.dc.routes.event.parse.method.IParseDetail
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.model.Route
import java.util.concurrent.CountDownLatch

/**
 * Class for parsing data from different sources
 */
class QueryParseDetail(
        private val route: Route,
        private val isNetwork: Boolean,
        private var barrier: CountDownLatch
) : Thread() {

    // Output data
    var listRoute: ListRoute? = null
        private set
    var isDownload: Boolean = false
        private set

    override fun run() {
        // Calculate methods for parsing
        val listParseRailway = mutableListOf<IParseDetail>()
        listParseRailway.add(DBParseDetail())
        if (isNetwork)
            listParseRailway.add(HTMLParseDetail())
        // Execute parsing
        for (parseDetail in listParseRailway) {
            listRoute = parseDetail.get(route)
            if (listRoute != null && !listRoute!!.isEmpty)
                break
            isDownload = isNetwork
        }
        // Если есть барьер, то ослабляем его
        barrier.countDown()
    }
}