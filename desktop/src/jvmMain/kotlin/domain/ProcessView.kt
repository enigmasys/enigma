package domain

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import common.model.ProcessState
import common.model.process.ProcessOwned
import common.model.process.ProcessOwnedItem
import common.services.PremonitionProcessServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

@Component
class ProcessView(_context: ConfigurableApplicationContext) {
    var processObj: PremonitionProcessServiceImpl = _context.getBean(PremonitionProcessServiceImpl::class.java)
    
    var processList = mutableStateListOf<String>()
//    var processMap = mutableStateMapOf<String,List<String>>()
    var processMap = mutableStateMapOf<String,List<ProcessOwnedItem>>()
    var coroutineScope = CoroutineScope(Dispatchers.Main)

    var processState = mutableStateMapOf<String, ProcessState>()
//    private var isActive = false

    fun start(){
//        if(this@Process.isActive) return

        coroutineScope.launch {
//            this@Process.isActive = true
//            while(this@Process.isActive) {
//            delay(1000L)
//                processList = test2() as SnapshotStateList<String>

            var tmp = test2()
            tmp?.groupBy { it.processType }?.entries?.map { it.value.map { it.processId } }?.flatten()
                ?.let { processList.addAll(it) }

//            tmp?.groupBy { it.processType }?.map { processMap.put(it.key,it.value.map { it.processId })}
            tmp?.groupBy { it.processType }?.map { processMap.put(it.key,it.value)}


//            processList.removeAll { it.length >= 0 }

//            processList.addAll()
//                isActive = false
//            }

        }
    }

    fun test2(): ProcessOwned? {
        val sections = mutableListOf("")

//        coroutineScope.launch {
                var tmp:ProcessOwned? = processObj.getListofProcesses()
//                println(tmp)
        return tmp
//        tmp.groupBy { it.processType }.entries
//        tmp.groupBy { it.processType }.entries.map { it.value.map { it.processId } }.flatten()
//            }

//        repeat(10){
//            sections.add((1..10)
//                .map { i -> kotlin.random.Random.nextInt(0, 10) }
//                .joinToString(""))
//        }
    }

    fun getProcessState(id: String)  {
//        if(!processState.keys.contains(id))
            processObj.getProcessState(id)?.let { processState.put(id, it) }
    }
}