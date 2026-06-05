package com.example.bgls.DataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApprovalModel(
    val slNo: Int?,
    val custGroup: String?,
    val appRefNo: String?,
    val accountType: String?,
    val customerName: String?,
    val nationalId: String?,
    val status: String?
)
data class ApprovalListResponse(
    val customerRequest: List<ApprovalApiItem>
)

data class ApprovalApiItem(
    @SerializedName("appl_ref_no") val applRefNo: String,
    @SerializedName("ca_customer_type") val custGroup: String,
    @SerializedName("ca_acct_type") val accountType: String,
    @SerializedName("ca_preferred_name") val preferredName: String?,
    @SerializedName("ca_full_name_1") val fullName: String?,
    @SerializedName("ca_idenditification_number") val nationalId: String,
    @SerializedName("status") val status: String,
    val corporateName: String? = null
)
data class KycItem(
    @SerializedName("appl_ref_no") val applRefNo: String,
    @SerializedName("ca_customer_type") val customerGroup: String,
    @SerializedName("ca_acct_type") val accountType: String,
    @SerializedName("ca_preferred_name") val preferredName: String?,
    @SerializedName("ca_full_name_1") val fullName: String?,
    @SerializedName("ca_idenditification_number") val nationalId: String
)
data class KycListResponse(
    val kyc: List<KycItem>
)
data class ApprovalDetailResponse(
    val customerRequest: CustomerRequestDetail
)

data class CustomerRequestDetail(
    val appl_ref_no: String,
    val ca_customer_type: String,
    val ca_preferred_name: String?,
    val ca_full_name_1: String?,
    val status: String?
)

data class ApprovalViewResponse(
    @SerializedName("customerRequest") val customerRequest: CustomerRequestDetailFull,
    @SerializedName("loanDetails") val loanDetails: LoanDetails?,
    @SerializedName("paymentDetails") val paymentDetails: PaymentDetails?,
    @SerializedName("customerdata") val depositData: DepositData?,
    @SerializedName("DocumentList") val documentList: List<DocumentItem>?,
    @SerializedName("SIGNATURE") val signatureList: List<SignatureItem>?
)

data class CustomerRequestDetailFull(
    val appl_ref_no: String,
    val rec_no: String,
    val ca_solid: String?,
    val branch_desc: String?,
    val ca_customer_type: String?,
    val ca_acct_type: String?,           // Account type (LA/TD/etc)
    val ca_first_name: String?,
    val mid_name: String?,
    val ca_last_name: String?,
    val ca_preferred_name: String?,
    val ca_full_name_1: String?,          // Full name field from DB
    val ca_date_of_birth: String?,
    val ca_mobile_number: String?,
    val ca_countrycode_1: String?,
    val ca_passport_number: String?,
    val ca_idenditification_number: String?,
    val cif_id: String?,
    val la_customer_type: String?,
    val ca_saluation: String?,
    val ca_occupation1: String?,
    val ca_gender: String?,
    val ca_martial_staus: String?,
    val annual_income: String?,
    val monthly_income: String?,
    val loan_obligations: String?,
    val family_maintenance: String?,
    val ca_email_id: String?,
    val ca_address_type: String?,
    val ca_house_no: String?,
    val ca_street_no: String?,
    val ca_street_name: String?,
    val ca_country: String?,
    val ca_state: String?,
    val ca_city: String?,
    val ca_postal_code: String?,
    val ca_address_validation_form: String?,
    val ca_nationality: String?,
    val ca_country_of_birth: String?,
    val countryOrigin: String?,
    val ca_schemetype: String?,
    val ca_scheme_code: String?,
    val la_loan_accountno: String?,
    val td_deposit_accountno: String?,
    val la_glcode: String?,
    val la_gldesc: String?,
    val la_glshcode: String?,
    val la_glshdesc: String?,
    val la_date_loan: String?,            // Date of loan (was missing)
    val la_loan_sanctioned: String?,
    val la_margin: String?,
    val la_drawing_limit: String?,        // Drawing limit (was missing)
    val la_disbursement: String?,         // Disbursement amount (was missing)
    val la_outstanding: String?,          // Loan outstanding (was missing)
    val la_recovery_method: String?,
    val la_remarks: String?,
    val la_loan_period: String?,
    val la_expiry_date: String?,          // Loan expiry date (was missing)
    val td_glcode: String?,
    val td_gldesc: String?,
    val td_glshcode: String?,
    val td_glshdesc: String?,
    val td_date_deposit: String?,         // Deposit date (was missing)
    val td_deposit_amt: String?,
    val td_period: String?,
    val td_currency: String?,             // Deposit currency (was missing)
    val td_rate_interest: String?,
    val td_interest_amt: String?,
    val td_maturity_amt: String?,
    val td_maturity: String?,             // Maturity date from CustomerRequest (was missing)
    val td_compounding_factor: String?,
    val ca_issue_date: String?,
    val ca_expiry_date: String?,
    val ca_non_resident: String?,
    val ca_staff_indicator: String?,
    val ca_trdfin: String?,
    val ca_minor_indicator: String?,
    val status: String?
)

data class LoanDetails(
    val loan_accountno: String?,
    val date_of_loan: String?,
    val loan_currency: String?,
    val effective_interest_rate: String?,
    val loan_sanctioned: Double?,
    val loan_period: Int?,
    val margin_limit: Double?,
    val drawing_limit: Double?,
    val loan_outstanding: Double?,
    val disbursement: Double?,
    val recovery_method: String?,
    val expiry_date: String?
)

data class PaymentDetails(
    val inst_id: String?,
    val inst_start_dt: String?,
    val no_of_inst: Int?,
    val maturity_flg: String?,
    val inst_freq: String?,
    val interest_frequency: String?,
    val inst_amount: Double?,
    val inst_pct: Double?
)

data class DepositData(
    val depo_actno: String?,
    val deposit_date: String?,
    val deposit_amt: String?,
    val currency: String?,
    val deposit_period: String?,
    val maturity_date: String?,
    val rate_of_int: String?,
    val int_amt: String?,
    val compounding_factor: String?,
    val interest_type: String?,
    val frequency: String?,
    val deposit_type: String?,
    val deposit_frequency: String?
)

data class DocumentItem(
    val document_type: String?,
    val document_code: String?,
    val document_type_desc: String?,
    val place_of_issue: String?,
    val unique_id: String?,
    val issue_date: String?,
    val expiry_date: String?,
    val file_name: String?
)


data class SignatureItem(
    val keyword: String?
)