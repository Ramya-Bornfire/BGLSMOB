package com.example.bgls.DataModels

data class CustomerRequest(
    var la_customer_type: String? = null,
    var cif_id: String? = null,
    var ca_solid: String? = null,
    var ca_acct_opendate: String? = null,
    var ca_remarks: String? = null,
    var shortName: String? = null,
    var ca_customer_type: String? = null,
    var ca_customer_type_1: String? = null,
    var ca_saluation: String? = null,
    var ca_first_name: String? = null,
    var mid_name: String? = null,
    var ca_last_name: String? = null,
    var ca_preferred_name: String? = null,
    var ca_occupation1: String? = null,
    var ca_gender: String? = null,
    var ca_martial_staus: String? = null,
    var ca_date_of_birth: String? = null,
    var ca_currency: String? = null,
    var loan_obligations: String? = null,
    var family_maintenance: String? = null,
    var ca_countrycode_1: String? = null,
    var ca_address_type: String? = null,
    var ca_house_no: String? = null,
    var ca_street_no: String? = null,
    var ca_street_name: String? = null,
    var ca_country: String? = null,
    var ca_state: String? = null,
    var ca_city: String? = null,
    var ca_postal_code: String? = null,
    var ca_address_validation_form: String? = null,
    var ca_nationality: String? = null,
    var ca_country_of_birth: String? = null,
    var countryOrigin: String? = null,
    var ca_email_id: String? = null,
    var ca_mobile_number: String? = null,
    var ca_phone_number: String? = null,
    
    // Corporate fields
    var constitution: String? = null,
    var corporateName: String? = null,
    var tradeName: String? = null,
    var certificate_incorporation: String? = null,
    var business_registration: String? = null,
    var date_incorporation: String? = null,
    var pbn: String? = null,
    var lno: String? = null,
    var fn: String? = null,
    var website: String? = null,
    var passno: String? = null,
    var nationalid: String? = null,
    
    // Other common fields
    var annual_income: String? = null,
    var monthly_income: String? = null,
    var branch_desc: String? = null,
    var ca_cif_id_1: String? = null
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        
        fun formatDate(dateString: String?): String? {
            if (dateString.isNullOrBlank()) return null
            return try {
                val parts = dateString.split("-")
                if (parts.size == 3 && parts[2].length == 4) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else {
                    dateString
                }
            } catch (e: Exception) {
                dateString
            }
        }

        la_customer_type?.let { map["la_customer_type"] = it }
        cif_id?.let { map["cif_id"] = it }
        ca_solid?.let { map["ca_solid"] = it }
        ca_acct_opendate?.let { map["ca_acct_opendate"] = formatDate(it) ?: it }
        ca_remarks?.let { map["ca_remarks"] = it }
        shortName?.let { map["shortName"] = it }
        ca_customer_type?.let { map["ca_customer_type"] = it }
        ca_customer_type_1?.let { map["ca_customer_type_1"] = it }
        ca_saluation?.let { map["ca_saluation"] = it }
        ca_first_name?.let { map["ca_first_name"] = it }
        mid_name?.let { map["mid_name"] = it }
        ca_last_name?.let { map["ca_last_name"] = it }
        ca_preferred_name?.let { map["ca_preferred_name"] = it }
        ca_occupation1?.let { map["ca_occupation1"] = it }
        ca_gender?.let { map["ca_gender"] = it }
        ca_martial_staus?.let { map["ca_martial_staus"] = it }
        ca_date_of_birth?.let { map["ca_date_of_birth"] = formatDate(it) ?: it }
        ca_currency?.let { map["ca_currency"] = it }
        loan_obligations?.let { map["loan_obligations"] = it }
        family_maintenance?.let { map["family_maintenance"] = it }
        ca_countrycode_1?.let { map["ca_countrycode_1"] = it }
        ca_address_type?.let { map["ca_address_type"] = it }
        ca_house_no?.let { map["ca_house_no"] = it }
        ca_street_no?.let { map["ca_street_no"] = it }
        ca_street_name?.let { map["ca_street_name"] = it }
        ca_country?.let { map["ca_country"] = it }
        ca_state?.let { map["ca_state"] = it }
        ca_city?.let { map["ca_city"] = it }
        ca_postal_code?.let { map["ca_postal_code"] = it }
        ca_address_validation_form?.let { map["ca_address_validation_form"] = formatDate(it) ?: it }
        ca_nationality?.let { map["ca_nationality"] = it }
        ca_country_of_birth?.let { map["ca_country_of_birth"] = it }
        countryOrigin?.let { map["countryOrigin"] = it }
        ca_email_id?.let { map["ca_email_id"] = it }
        ca_mobile_number?.let { map["ca_mobile_number"] = it }
        ca_phone_number?.let { map["ca_phone_number"] = it }
        
        constitution?.let { map["cons_name"] = it }
        corporateName?.let { map["cor_name"] = it }
        tradeName?.let { map["trade_name"] = it }
        certificate_incorporation?.let { map["cer_od_incop"] = it }
        business_registration?.let { map["buss_ref_no"] = it }
        date_incorporation?.let { map["doi"] = formatDate(it) ?: it }
        pbn?.let { map["pbn"] = it }
        lno?.let { map["lno"] = it }
        fn?.let { map["fn"] = it }
        website?.let { map["website"] = it }
        passno?.let { map["currency"] = it }
        nationalid?.let { map["national_id2"] = it }
        
        annual_income?.let { map["annual_income"] = it }
        monthly_income?.let { map["monthly_income"] = it }
        branch_desc?.let { map["branch_desc"] = it }
        ca_cif_id_1?.let { map["ca_cif_id_1"] = it }
        
        return map
    }
}
