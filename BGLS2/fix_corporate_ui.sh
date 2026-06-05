sed -i '' '/id="@+id\/etLoanPeriod"/a\
\
                            <!-- Missing Fields Added Below -->\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Deposit Account No" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etDepositAccountNo" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/readonly_background" android:enabled="false" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Date of Deposit" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etDateOfDeposit" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/readonly_background" android:enabled="false" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Date of Loan" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etDateOfLoan" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/readonly_background" android:enabled="false" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Deposit Amount" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etDepositAmount" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/edittext_background" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333" android:inputType="numberDecimal"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Deposit Period (Months)" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etDepositPeriod" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/edittext_background" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333" android:inputType="number"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Rate of Interest" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etRateOfInterest" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/edittext_background" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333" android:inputType="numberDecimal"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Maturity Date" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etMaturityDate" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/readonly_background" android:enabled="false" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Interest Amount" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etInterestAmount" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/readonly_background" android:enabled="false" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333"/>\
                            </LinearLayout>\
                            <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="6dp">\
                                <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="0.45" android:text="Maturity Amount" android:textSize="10sp" android:textStyle="bold" android:textColor="#333333"/>\
                                <EditText android:id="@+id/etMaturityAmount" android:layout_width="0dp" android:layout_height="36dp" android:layout_weight="0.55" android:background="@drawable/readonly_background" android:enabled="false" android:paddingHorizontal="6dp" android:textSize="10sp" android:textColor="#333333"/>\
                            </LinearLayout>\
' app/src/main/res/layout/activity_corporate_customer_account_opening.xml
