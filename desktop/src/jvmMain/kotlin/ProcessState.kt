//
import common.services.PremonitionProcessServiceImpl
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import org.springframework.stereotype.Component

@Component
class ProcessState(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
//    private val AuthServiceObj: AuthService
)
{
    var listofProcesses = true
    fun call(): Int {

//        parent?.let { it ->
//            if (it.token?.length?.compareTo(0) ?:  0  > 0){
//                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
//            } }

        when{
            listofProcesses ->{
                val result =  ProcessServiceObj.getListofProcesses()
                if (result != null) {
//                    prettyPrint(result)
                    prettyJsonPrint(result)
                }
            }
            else -> 0
        }
        return 0
    }

}