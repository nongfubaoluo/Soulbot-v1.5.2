package com.loveadvisor.android.memory
import com.google.gson.Gson
import com.loveadvisor.android.storage.AndroidDocumentStore
import com.loveadvisor.core.memory.*
import com.loveadvisor.core.model.*
import com.loveadvisor.core.knowledge.KnowledgeDocument
data class SavedChat(val objectId:String,val messages:List<ChatMessage>,val createdAt:Long=System.currentTimeMillis())
data class ProfileAnalysisRecord(val id:String,val platform:String,val visibleText:String,val analysis:String,val createdAt:Long=System.currentTimeMillis())
data class ProfileMatchRecord(val id:String,val targetProfileId:String,val platform:String,val score:Int,val suggestedMessage:String,val detail:String,val createdAt:Long=System.currentTimeMillis())
data class ProfileData(val self:RelationshipProfile=RelationshipProfile("self","本人"),val objects:List<RelationshipProfile> = emptyList(),val notes:List<KnowledgeDocument> = emptyList(),val history:List<SavedChat> = emptyList(),val profileAnalyses:List<ProfileAnalysisRecord> = emptyList(),val selfHomepage:ProfileAnalysisRecord?=null,val latestTargetHomepage:ProfileAnalysisRecord?=null,val matchRecords:List<ProfileMatchRecord> = emptyList())
class ProfileStore(private val store:AndroidDocumentStore) {
    private val gson=Gson()
    @Synchronized fun load():ProfileData=store.read("profiles")?.let { raw -> gson.fromJson(raw,ProfileData::class.java).let { it.copy(profileAnalyses=it.profileAnalyses ?: emptyList(),matchRecords=it.matchRecords ?: emptyList()) } } ?: ProfileData()
    @Synchronized fun save(data:ProfileData)=store.write("profiles",gson.toJson(data))
    fun find(id:String?)=load().objects.find { it.id==id }
    @Synchronized fun withExisting(id:String?,operation:(RelationshipProfile)->Unit) { load().objects.find { it.id==id }?.let(operation) }
    @Synchronized fun put(profile:RelationshipProfile) {
        val data=load(); save(if(profile.id=="self") data.copy(self=profile) else data.copy(objects=data.objects.filter { it.id!=profile.id }+profile))
    }
    @Synchronized fun delete(id:String) { val d=load(); save(d.copy(objects=d.objects.filter { it.id!=id },history=d.history.filter { it.objectId!=id })) }

    @Synchronized fun rememberProfileAnalysis(platform:String,visibleText:String,analysis:String) {
        val d=load(); val item=ProfileAnalysisRecord(java.util.UUID.randomUUID().toString(),platform,visibleText.take(12000),analysis.take(16000)); save(d.copy(profileAnalyses=(d.profileAnalyses+item).takeLast(100),latestTargetHomepage=item))
    }
    @Synchronized fun rememberTargetHomepage(platform:String,visibleText:String):ProfileAnalysisRecord {
        val d=load();val item=ProfileAnalysisRecord(java.util.UUID.randomUUID().toString(),platform,visibleText.take(12000),"");save(d.copy(latestTargetHomepage=item));return item
    }
    @Synchronized fun saveSelfHomepage(platform:String,visibleText:String,analysis:String):ProfileAnalysisRecord {
        val d=load();val item=ProfileAnalysisRecord(java.util.UUID.randomUUID().toString(),platform,visibleText.take(12000),analysis.take(16000));save(d.copy(selfHomepage=item));return item
    }
    @Synchronized fun rememberProfileMatch(platform:String,score:Int,suggestedMessage:String,detail:String):ProfileMatchRecord {
        val d=load();val item=ProfileMatchRecord(java.util.UUID.randomUUID().toString(),d.latestTargetHomepage?.id.orEmpty(),platform,score.coerceIn(0,100),suggestedMessage.take(1000),detail.take(4000));save(d.copy(matchRecords=(d.matchRecords+item).takeLast(500)));return item
    }
    @Synchronized fun rememberChat(id:String,snapshot:ChatSnapshot) {
        val d=load(); if(d.objects.none { it.id==id }) return
        val previous=d.history.filter { it.objectId==id }
        if(previous.lastOrNull()?.messages==snapshot.messages) return
        save(d.copy(history=d.history.filter { it.objectId!=id }+(previous+SavedChat(id,snapshot.messages.takeLast(80))).takeLast(30)))
    }
}
class EncryptedMemoryPersistence(private val store:AndroidDocumentStore):MemoryPersistence {
    private val gson=Gson()
    override fun load():MemoryState=store.read("memory")?.let { gson.fromJson(it,MemoryState::class.java) } ?: MemoryState()
    override fun save(state:MemoryState)=store.write("memory",gson.toJson(state))
}
