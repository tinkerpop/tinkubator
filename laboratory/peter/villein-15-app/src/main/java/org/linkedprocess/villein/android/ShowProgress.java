package org.linkedprocess.villein.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.linkedprocess.demos.primes.PrimeFinderAsynchWithProgress;
import org.linkedprocess.smack.AndroidProviderManager;
import org.linkedprocess.villein.patterns.ScatterGatherPattern;
import org.linkedprocess.villein.proxies.JobProxy;
import org.linkedprocess.villein.proxies.ResultHolder;
import org.linkedprocess.villein.proxies.VmProxy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShowProgress extends Activity {

	private Handler handler;
	private LinearLayout.LayoutParams tlp;
	private LinearLayout l;
	private TextView status;

	@Override
	protected void onCreate(final Bundle context) {
		super.onCreate(context);
		handler = new Handler();
		tlp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		status = new TextView(this);
		Button start = new Button(this);
		start.setText("start");
		start.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					findPrimes(context);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		l.addView(status);
		l.addView(start);
		setContentView(l);

	}

	private void findPrimes(Bundle context) throws Exception {
		Thread findPrimesThread = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub

				AndroidProviderManager.init(ShowProgress.this);
				Bundle extras = getIntent().getExtras();
				status("connecting");
				try {
					PrimeFinderAsynchWithProgress.init(extras
							.getString(Villein.USERNAME), extras
							.getString(Villein.PASSWORD), extras
							.getString(Villein.SERVER), extras
							.getInt(Villein.PORT), extras
							.getString(Villein.FARM_PASSWORD));
					// ////////////// ALLOCATE FARMS
					status("waiting for farms ...");
					PrimeFinderAsynchWithProgress.allocateFarms(1);

					// ////////////// SPAWN VIRTUAL MACHINES ON ALLOCATED FARMS

					status("spawning " + extras.getInt(Villein.NUMBER_OF_VMS)
							+ " VMs");
					PrimeFinderAsynchWithProgress.spawnVms(extras
							.getInt(Villein.NUMBER_OF_VMS));

					Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
					for (ResultHolder<VmProxy> proxy : PrimeFinderAsynchWithProgress.vmProxies) {
						final ProgressBar progress = new ProgressBar(
								ShowProgress.this, null,
								android.R.attr.progressBarStyleHorizontal);
						progress.setLayoutParams(tlp);
						final TextView vm = new TextView(ShowProgress.this);
						vm.setText(proxy.getSuccess().getVmId());
						handler.post(new Runnable() {
							public void run() {
								l.addView(vm);
								l.addView(progress);

							}
						});
						VmPollVisualProgressTask task = new VmPollVisualProgressTask(
								proxy.getSuccess(), 1.0, 500, handler, progress);
						tasks.add(Executors.callable(task));
					}
					// ////////////// DISTRIBUTE PRIME FINDER FUNCTION
					// DEFINITION

					status("deploying jobs .");
					PrimeFinderAsynchWithProgress.scatterPrimeCalcFunction();
					// ////////////// DISTRIBUTE VM BINDINGS
					status("deploying jobs ..");
					PrimeFinderAsynchWithProgress.scatterVmBindings();
					// ////////////// DISTRIBUTE PRIME FINDER FUNCTION CALLS
					status("deploying jobs ...");
					PrimeFinderAsynchWithProgress
							.scatterWorkerJob(
									1,
									10000,
									System.currentTimeMillis(),
									new org.linkedprocess.villein.Handler<Map<VmProxy, JobProxy>>() {

										public void handle(
												Map<VmProxy, JobProxy> t) {
											// TODO Auto-generated method stub
											// ////////////// TERMINATE ALL
											// SPAWNED VIRTUAL MACHINES

											System.out
													.println("Terminating virtual machines...");
											ScatterGatherPattern
													.scatterTerminateVm(PrimeFinderAsynchWithProgress.vmJobMap
															.keySet());

											// ////////////// SORT AND DISPLAY
											// JOB RESULT PRIME
											// VALUES

											System.out
													.println("Gathering find primes function results...");
											ArrayList<Integer> primes = new ArrayList<Integer>();
											for (JobProxy jobStruct : PrimeFinderAsynchWithProgress.vmJobMap
													.values()) {
												if (jobStruct.wasSuccessful()) {
													for (String primeString : jobStruct
															.getResult()
															.replace("[", "")
															.replace("]", "")
															.split(",")) {
														if (!primeString.trim()
																.equals(""))
															primes
																	.add(Integer
																			.valueOf(primeString
																					.trim()));
													}
												} else {
													System.out
															.println("Job "
																	+ jobStruct
																			.getJobId()
																	+ " was unsuccessful.");
												}
											}
											Collections.sort(primes);
											// System.out.println("Running time: "
											// + (System.currentTimeMillis() -
											// startTime)
											// / 1000.0f + " seconds.");
											System.out
													.println("Prime finder results: "
															+ primes);
											final TextView result = new TextView(
													ShowProgress.this);
											result.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
													LayoutParams.FILL_PARENT));
											result.setText(primes + "");
											result.setLines(10);
											handler.post(new Runnable() {

												public void run() {
													l.addView(result);

												}
											});
										}

									});
					status("working ...");
					PrimeFinderAsynchWithProgress.startPolling(tasks);
					status("finishied");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		findPrimesThread.start();

	}

	private void status(final String string) {
		handler.post(new Runnable() {

			public void run() {
				status.setText(string);

			}
		});
	}
}
