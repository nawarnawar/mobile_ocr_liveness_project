@file:Suppress("UNCHECKED_CAST")

package com.regula.documentreader

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.regula.documentreader.Helpers.Companion.getResultTypeTranslation
import com.regula.documentreader.Helpers.Companion.replaceFragment
import com.regula.documentreader.Helpers.Companion.themeColor
import com.regula.documentreader.Status.Companion.FAIL
import com.regula.documentreader.Status.Companion.SUCCESS
import com.regula.documentreader.Status.Companion.UNDEFINED
import com.regula.documentreader.api.enums.LCID
import com.regula.documentreader.api.enums.eCheckResult.*
import com.regula.documentreader.api.enums.eRFID_DataFile_Type
import com.regula.documentreader.api.enums.eRFID_ErrorCodes.*
import com.regula.documentreader.api.enums.eVisualFieldType
import com.regula.documentreader.api.results.DocumentReaderComparison
import com.regula.documentreader.api.results.DocumentReaderResults
import com.regula.documentreader.api.results.DocumentReaderValidity
import com.regula.documentreader.databinding.ActivityResultsBinding
import com.regula.documentreader.databinding.FragmentResultsBinding
import com.regula.documentreader.databinding.FragmentRvBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.*

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding
    private lateinit var resultsFragment: ResultsTabFragment
    private lateinit var compareFragment: ResultsTabFragment
    private lateinit var rfidFragment: ResultsTabFragment
    private var selectedTabIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Helpers.opaqueStatusBar(binding.root)
        binding.backBtn.setOnClickListener { finish() }
        binding.directBtn.setOnClickListener {
            startActivity(Intent(this, DirectResultsActivity::class.java))
        }

        // Fix: Convert Int to eCheckResult enum
        when(results.status.overallStatus) {
            CH_CHECK_OK.value -> binding.overAllResultImage.setImageResource(R.drawable.reg_icon_check_ok)
            CH_CHECK_WAS_NOT_DONE.value -> binding.overAllResultImage.setImageResource(R.drawable.reg_icon_no_check)
            else -> binding.overAllResultImage.setImageResource(R.drawable.reg_icon_check_fail)
        }

        // Envoi des résultats au serveur
        results.rawResult?.let { rawData ->
            val jsonString = rawData
            sendToServer(jsonString)
        } ?: run {
            Toast.makeText(this, "Aucune donnée JSON disponible", Toast.LENGTH_SHORT).show()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab!!.position) {
                    0 -> {
                        selectedTabIndex = 0
                        replaceFragment(
                            resultsFragment,
                            this@ResultsActivity,
                            R.id.results_compare_rfid
                        )
                    }
                    1 -> {
                        selectedTabIndex = 1
                        replaceFragment(
                            compareFragment,
                            this@ResultsActivity,
                            R.id.results_compare_rfid
                        )
                    }
                    2 -> {
                        selectedTabIndex = 2
                        replaceFragment(
                            rfidFragment,
                            this@ResultsActivity,
                            R.id.results_compare_rfid
                        )
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        if (savedInstanceState == null) {
            resultsGroupedAttributes = initResults()
            compareGroupedAttributes = initCompare()
            rfidGroupedAttributes = initRfidData()
            resultsFragment = ResultsTabFragment.newInstance(RESULTS)
            compareFragment = ResultsTabFragment.newInstance(COMPARE)
            rfidFragment = ResultsTabFragment.newInstance(RFID)
            replaceFragment(resultsFragment, this, R.id.results_compare_rfid)
            binding.tabLayout.getTabAt(selectedTabIndex)!!.select()
        } else {
            resultsFragment =
                savedInstanceState.getSerializable("resultsFragment") as ResultsTabFragment
            compareFragment =
                savedInstanceState.getSerializable("compareFragment") as ResultsTabFragment
            rfidFragment = savedInstanceState.getSerializable("rfidFragment") as ResultsTabFragment

            resultsFragment.numberPickerIndex = savedInstanceState.getInt("resultsIndex")
            compareFragment.numberPickerIndex = savedInstanceState.getInt("compareIndex")
            rfidFragment.numberPickerIndex = savedInstanceState.getInt("rfidIndex")

            selectedTabIndex = savedInstanceState.getInt("selectedTabIndex")
        }
        binding.tabLayout.getTabAt(selectedTabIndex)!!.select()

        // Lancer automatiquement la vérification faciale après OCR
        val documentImage = results.graphicResult?.fields?.firstOrNull()?.bitmap
        if (documentImage != null) {
            val intent = Intent(this, FaceVerificationActivity::class.java)
            val stream = ByteArrayOutputStream()
            documentImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            intent.putExtra(FaceVerificationActivity.EXTRA_DOCUMENT_PHOTO, byteArray)
            startActivityForResult(intent, FACE_VERIFICATION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Photo du document non trouvée pour vérification faciale.", Toast.LENGTH_SHORT).show()
        }


        if (compareGroupedAttributes.isEmpty())
            turnTabOff(1)
        if (rfidGroupedAttributes.isEmpty())
            turnTabOff(2)
    }


    private fun sendToServer(jsonData: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonData.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://192.168.1.107:3000/api/document") // Remplacez par votre URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ResultsActivity,
                        "Erreur d'envoi: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ResultsActivity,
                            "Données envoyées avec succès",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ResultsActivity,
                            "Erreur serveur: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun initResults(): List<GroupedAttributes> {
        val pickerData = mutableListOf<GroupedAttributes>()
        val attributes = mutableListOf<Attribute>()

        results.textResult?.fields?.forEach {
            val name = it.getFieldName(this)
            for (value in it.values) {
                val valid = getValidity(value.field.validityList, value.sourceType)
                val item = Attribute(
                    name!!,
                    value.value,
                    it.lcid,
                    value.pageIndex,
                    valid,
                    value.sourceType
                )
                attributes.add(item)
            }
        }

        results.graphicResult?.fields?.forEach {
            val name = it.getFieldName(this) + " [${it.pageIndex}]"
            val image = it.bitmap
            val item = Attribute(name, "", source = it.sourceType, image = image)
            attributes.add(item)
        }

        val types = attributes.map { it.source }.toSet()
        for (type in types) {
            val typed = attributes.filter { it.source == type }.toMutableList()
            val group = GroupedAttributes(getResultTypeTranslation(type!!), typed)
            pickerData.add(group)
        }
        pickerData.sortBy { it.type }

        return pickerData
    }

    private fun getValidity(
        list: List<DocumentReaderValidity>,
        type: Int?
    ): Int {
        for (validity in list) {
            if (validity.sourceType == type)
                return validity.status
        }
        return CH_CHECK_WAS_NOT_DONE.value
    }

    private fun initCompare(): List<GroupedAttributes> {
        var pickerData = mutableListOf<GroupedAttributes>()

        if(results.textResult == null)
            return pickerData

        val values = results.textResult!!.fields.map { it.values }.flatten()
        val comparisonTypes = values.map { it.sourceType }.toSet()

        val typePairs = combinationsFrom(comparisonTypes.toMutableList(), 2)

        for (pair in typePairs) {
            val groupType =
                "${getResultTypeTranslation(pair[1])} - ${getResultTypeTranslation(pair[0])}"
            val comparisonGroup = GroupedAttributes(groupType, mutableListOf(), pair[1], pair[0])
            pickerData.add(comparisonGroup)
        }

        for (field in results.textResult!!.fields)
            for (comparison in field.comparisonList)
                tryToAddValueToGroup(
                    field.getFieldName(this)!!,
                    comparison,
                    pickerData
                )

        for (group in pickerData)
            group.items = group.items.toSet().toMutableList()
        pickerData = pickerData.filter { it.items.size > 0 }.toMutableList()

        if (pickerData.size == 0)
            turnTabOff(1)

        return pickerData
    }

    private fun tryToAddValueToGroup(
        name: String,
        comparison: DocumentReaderComparison,
        groups: MutableList<GroupedAttributes>
    ) {
        for (index in groups.indices) {
            val group = groups[index]
            if (group.comparisonLHS == null || group.comparisonRHS == null) return
            val sourceEquality =
                comparison.sourceTypeRight == group.comparisonLHS || comparison.sourceTypeRight == group.comparisonRHS
            val targetEquality = comparison.sourceTypeLeft == group.comparisonLHS || comparison.sourceTypeLeft == group.comparisonRHS
            if (sourceEquality && targetEquality) {
                val item =
                    Attribute(name, null, valid = comparison.status, source = comparison.sourceTypeLeft)
                groups[index].items.add(item)
                break
            }
        }
    }

    private fun <T> combinationsFrom(
        elements: MutableList<T>,
        taking: Int
    ): MutableList<MutableList<T>> {
        if (elements.size < taking) return mutableListOf()
        if (elements.isEmpty() || taking <= 0) return mutableListOf(mutableListOf())
        if (taking == 1) return elements.map { mutableListOf(it) }.toMutableList()

        val combinations = mutableListOf<MutableList<T>>()
        while (elements.size > 1) {
            val element = elements[0]
            elements.removeFirst()
            combinations += combinationsFrom(elements, taking - 1).map {
                (mutableListOf(element) + it).toMutableList()
            }
        }

        return combinations
    }

    private fun initRfidData(): List<GroupedAttributes> {
        val pickerData = mutableListOf<GroupedAttributes>()

        if (results.rfidSessionData == null)
            return pickerData

        val dataGroup = GroupedAttributes("Data Groups", mutableListOf())
        results.rfidSessionData!!.applications.forEach {
            it.files.forEach { it1 ->
                if (it1.readingStatus == RFID_ERROR_NOT_AVAILABLE.toLong())
                    return pickerData
                val attribute = Attribute(
                    eRFID_DataFile_Type.getTranslation(this, it1.type),
                    rfidStatus = it1.pAStatus.toInt()
                )
                dataGroup.items.add(attribute)
            }
        }
        if (dataGroup.items.isNotEmpty())
            pickerData.add(dataGroup)

        val statusGroup = GroupedAttributes("Data Status", items = mutableListOf())
        var attribute =
            Attribute("AA", checkResult = results.status.detailsRFID.aa)
        statusGroup.items.add(attribute)
        attribute = Attribute("BAC", checkResult = results.status.detailsRFID.bac)
        statusGroup.items.add(attribute)
        attribute = Attribute("CA", checkResult = results.status.detailsRFID.ca)
        statusGroup.items.add(attribute)
        attribute = Attribute("PA", checkResult = results.status.detailsRFID.pa)
        statusGroup.items.add(attribute)
        attribute =
            Attribute("PACE", checkResult = results.status.detailsRFID.pace)
        statusGroup.items.add(attribute)
        attribute = Attribute("TA", checkResult = results.status.detailsRFID.ta)
        statusGroup.items.add(attribute)

        if (statusGroup.items.size > 0)
            pickerData.add(statusGroup)

        return pickerData
    }

    private fun turnTabOff(tab: Int) {
        binding.tabLayout.getTabAt(tab)!!.view.isClickable = false
        binding.tabLayout.getTabAt(tab)!!.view.background =
            ResourcesCompat.getDrawable(resources, R.drawable.rounded_gray, theme)
        (binding.tabLayout.getTabAt(tab)!!.view.getChildAt(1) as TextView).setTextColor(Color.GRAY)

        if (!binding.tabLayout.getTabAt(1)!!.view.isClickable && !binding.tabLayout.getTabAt(2)!!.view.isClickable)
            binding.tabLayout.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_gray, theme)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putSerializable("resultsFragment", resultsFragment)
        savedInstanceState.putSerializable("compareFragment", compareFragment)
        savedInstanceState.putSerializable("rfidFragment", rfidFragment)

        savedInstanceState.putInt("resultsIndex", resultsFragment.numberPickerIndex)
        savedInstanceState.putInt("compareIndex", compareFragment.numberPickerIndex)
        savedInstanceState.putInt("rfidIndex", rfidFragment.numberPickerIndex)

        savedInstanceState.putInt("selectedTabIndex", selectedTabIndex)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FACE_VERIFICATION_REQUEST_CODE) {
            if (resultCode == FaceVerificationActivity.RESULT_VERIFICATION_SUCCESS) {
                Toast.makeText(this, "Vérification faciale terminée avec succès !", Toast.LENGTH_LONG).show()
            } else if (resultCode == FaceVerificationActivity.RESULT_VERIFICATION_FAILED) {
                Toast.makeText(this, "Vérification faciale échouée.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        lateinit var results: DocumentReaderResults
        var resultsGroupedAttributes: List<GroupedAttributes> = listOf()
        var compareGroupedAttributes: List<GroupedAttributes> = listOf()
        var rfidGroupedAttributes: List<GroupedAttributes> = listOf()

        const val RESULTS = 0
        const val COMPARE = 1
        const val RFID = 2
        const val FACE_VERIFICATION_REQUEST_CODE = 1001
    }
}

class ResultsTabFragment : Fragment(), Serializable {
    private var pickerDataReference = 0
    var numberPickerIndex: Int = 0

    companion object {
        fun newInstance(pickerData: Int): ResultsTabFragment {
            val instance = ResultsTabFragment()
            instance.pickerDataReference = pickerData
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, vg: ViewGroup?, bundle: Bundle?): View {
        val binding = FragmentResultsBinding.inflate(inflater, vg, false)

        pickerDataReference = bundle?.getInt("pickerDataReference") ?: pickerDataReference
        val pickerData = when (pickerDataReference) {
            ResultsActivity.RESULTS -> ResultsActivity.resultsGroupedAttributes
            ResultsActivity.COMPARE -> ResultsActivity.compareGroupedAttributes
            else -> ResultsActivity.rfidGroupedAttributes
        }
        if (pickerData.isEmpty())
            return binding.root

        val fragments = mutableListOf<GroupFragment>()
        for (groupedAttribute in pickerData)
            fragments.add(GroupFragment.newInstance(groupedAttribute))

        binding.resultsPicker.maxValue = pickerData.size - 1
        binding.resultsPicker.wrapSelectorWheel = false
        binding.resultsPicker.displayedValues = pickerData.map { it.type }.toTypedArray()
        binding.resultsPicker.setOnValueChangedListener { _, _, newVal ->
            numberPickerIndex = newVal
            binding.resultsPicker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            replaceFragment(
                fragments[newVal],
                requireActivity(),
                R.id.fragment_rv_results
            )
        }

        if (pickerDataReference == ResultsActivity.RESULTS && bundle != null)
            numberPickerIndex = bundle.getInt("numberPickerIndex")
        binding.resultsPicker.value = numberPickerIndex
        replaceFragment(
            fragments[numberPickerIndex],
            requireActivity(),
            R.id.fragment_rv_results
        )

        return binding.root
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("numberPickerIndex", numberPickerIndex)
        savedInstanceState.putInt("pickerDataReference", pickerDataReference)
    }
}

class GroupFragment : Fragment() {
    private val groupedAttributes: GroupedAttributes
        get() = requireArguments().getSerializable("groupedAttributes") as GroupedAttributes

    companion object {
        fun newInstance(groupedAttributes: GroupedAttributes): GroupFragment {
            val args = Bundle()
            args.putSerializable("groupedAttributes", groupedAttributes as Serializable)
            val instance = GroupFragment()
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, vg: ViewGroup?, bundle: Bundle?): View {
        val binding = FragmentRvBinding.inflate(inflater, vg, false)
        val sectionsData = mutableListOf<Base>()

        var tag = 0
        val type = groupedAttributes.type.lowercase(Locale.ROOT)
        if (type.contains("mrz"))
            tag = 10
        else if (type.contains("visual"))
            tag = 20
        else if (type.contains("barcodes"))
            tag = 30
        else if (type.contains("data groups") || type.contains("data status"))
            tag = 40
        sectionsData.add(Section(groupedAttributes.type, tag))

        for (attribute in groupedAttributes.items) {
            when {
                attribute.image != null -> sectionsData.add(
                    Image(
                        attribute.name,
                        attribute.image!!
                    )
                )
                attribute.value != null -> {
                    var color = requireContext().themeColor(R.attr.colorOnSecondary)
                    if (attribute.valid == CH_CHECK_OK.value)
                        color = Color.GREEN
                    if (attribute.valid == CH_CHECK_ERROR.value)
                        color = Color.RED
                    context?.let {
                        attribute.lcid?.let { lcid ->
                            sectionsData.add(
                                TextResult(
                                    attribute.name,
                                    attribute.value!!,
                                    LCID.getTranslation(it, lcid),
                                    attribute.pageIndex!!,
                                    color
                                )
                            )
                        }
                    }
                }
                attribute.rfidStatus != null -> {
                    var value = UNDEFINED
                    if (attribute.rfidStatus == RFID_ERROR_FAILED)
                        value = FAIL
                    else if (attribute.rfidStatus == RFID_ERROR_NO_ERROR)
                        value = SUCCESS
                    sectionsData.add(Status(attribute.name, value))
                }
                attribute.checkResult != null -> {
                    var value = UNDEFINED
                    if (attribute.checkResult == 0)
                        value = FAIL
                    else if (attribute.checkResult == 1)
                        value = SUCCESS
                    sectionsData.add(Status(attribute.name, value))
                }
                groupedAttributes.comparisonLHS != null
                        && groupedAttributes.comparisonRHS != null -> {
                    var value = UNDEFINED
                    if (attribute.valid == CH_CHECK_ERROR.value)
                        value = FAIL
                    else if (attribute.valid == CH_CHECK_OK.value)
                        value = SUCCESS
                    sectionsData.add(Status(attribute.name, value))
                }
                else -> {
                    var value = SUCCESS
                    if (!attribute.equality)
                        value = FAIL
                    sectionsData.add(Status(attribute.name, value))
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = CommonRecyclerAdapter(sectionsData)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, 1))

        return binding.root
    }
}