/*
 * **************************************************************************
 * Molecular Dynamics - Particles under the microscope
 * **************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * **************************************************************************
 */

package com.mkulesh.mmd.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.mkulesh.mmd.R;
import com.mkulesh.mmd.SettingsActivity;
import com.mkulesh.mmd.model.Constants.BoundaryConditionType;
import com.mkulesh.mmd.model.Constants.CalculationType;
import com.mkulesh.mmd.model.Constants.EnergyNormType;
import com.mkulesh.mmd.model.Constants.PotentialType;
import com.mkulesh.mmd.potentials.BasePotential;
import com.mkulesh.mmd.utils.CompatUtils;
import com.mkulesh.mmd.utils.ThreadContol;
import com.mkulesh.mmd.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Class that implements an atom set
 */
public class AtomSet implements Parcelable
{

    /**
     * State attributes to be stored in Parcel
     */
    protected PhysicalArea area = new PhysicalArea(); // original physical area
    protected PhysicalArea viewPort = new PhysicalArea(); // zoomed area
    protected ArrayList<Atom> atoms = new ArrayList<Atom>(); // atoms
    public Potential potential = new Potential(); // used potential
    public double atomRadius = 1.0; // radius of the atom
    public double atomMass = 55.847; // mass of the atom (in atomic mass unit)
    public double gravity = 0.0; // gravity constant
    public double thermalChange = 0; // thermal change coefficient (in percent [-1% ... +1%])
    public double timeStep = 5.0; // time step in femtosecond
    public EnergyNormType energyNorm = EnergyNormType.FULL_ENERGY; // how to norm energy within next step calculation
    public BoundaryConditionType boundaryCondition = BoundaryConditionType.REFLECTION;
    public double ePotenz = 0.0, eKinetic = 0.0, temperature = 0.0;
    public String atomImage = "res/raw/atom_blue_red.svg";

    /**
     * Parcelable interface
     */
    public AtomSet(Parcel in)
    {
        super();
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(area, flags);
        dest.writeParcelable(viewPort, flags);
        dest.writeTypedList(atoms);
        dest.writeString(potential.getType().toString());
        dest.writeDouble(atomRadius);
        dest.writeDouble(atomMass);
        dest.writeDouble(gravity);
        dest.writeDouble(thermalChange);
        dest.writeDouble(timeStep);
        dest.writeString(energyNorm.toString());
        dest.writeString(boundaryCondition.toString());
        dest.writeDouble(ePotenz);
        dest.writeDouble(eKinetic);
        dest.writeDouble(temperature);
        dest.writeString(atomImage);
    }

    private void readFromParcel(Parcel in)
    {
        area = in.readParcelable(PhysicalArea.class.getClassLoader());
        viewPort = in.readParcelable(PhysicalArea.class.getClassLoader());
        in.readTypedList(atoms, Atom.CREATOR);
        potential.setType(PotentialType.valueOf(in.readString()));
        atomRadius = in.readDouble();
        atomMass = in.readDouble();
        gravity = in.readDouble();
        thermalChange = in.readDouble();
        timeStep = in.readDouble();
        energyNorm = EnergyNormType.valueOf(in.readString());
        boundaryCondition = BoundaryConditionType.valueOf(in.readString());
        ePotenz = in.readDouble();
        eKinetic = in.readDouble();
        temperature = in.readDouble();
        atomImage = in.readString();
    }

    public static final Parcelable.Creator<AtomSet> CREATOR = new Parcelable.Creator<AtomSet>()
    {
        public AtomSet createFromParcel(Parcel in)
        {
            return new AtomSet(in);
        }

        public AtomSet[] newArray(int size)
        {
            return new AtomSet[size];
        }
    };

    /**
     * Private attributes to be created internally
     */
    private Random rand = new Random(); // used to generate random coordinates and velocities
    private SolverBase solver = null;
    private ArrayList<Atom> backupedAtoms = new ArrayList<Atom>();
    private ThreadContol pauseHandler = null;
    private long calculationTime = 0;

    /**
     * Default constructor
     */
    public AtomSet()
    {
        super();
    }

    /**
     * Copy constructor
     */
    public AtomSet(AtomSet set)
    {
        super();
        area.assign(set.area);
        viewPort.assign(set.viewPort);
        for (Atom a : set.atoms)
        {
            atoms.add(new Atom(a));
        }
        potential.setType(set.potential.getType());
        atomRadius = set.atomRadius;
        atomMass = set.atomMass;
        gravity = set.gravity;
        ePotenz = set.ePotenz;
        eKinetic = set.eKinetic;
        temperature = set.temperature;
        atomImage = set.atomImage;
        pauseHandler = set.pauseHandler;
        calculationTime = set.calculationTime;
    }

    /**
     * Procedure sets the pause handler object
     */
    public void setPauseHandler(ThreadContol pauseHandler)
    {
        this.pauseHandler = pauseHandler;
    }

    /**
     * Procedure returns actual visible physical area
     */
    public PhysicalArea getArea()
    {
        return area;
    }

    /**
     * Procedure returns actual visible physical area
     */
    public PhysicalArea getVewPort()
    {
        return viewPort;
    }

    /**
     * Procedure returns actual atom vector
     */
    public ArrayList<Atom> getAtoms()
    {
        return atoms;
    }

    /**
     * Procedure sets atom coordinate using limits of equidistance grid
     */
    public void fillGridCoordinates(int max_x, int max_y, boolean isDiag)
    {
        ViewUtils.Debug(this, "creating new atoms grid");
        atoms.clear();
        for (int x = 0; x < max_x; x++)
        {
            for (int y = 0; y < max_y; y++)
            {
                Atom a = new Atom();
                double dx = area.getDim().x / ((double) (max_x + 1));
                double dy = area.getDim().y / ((double) (max_y + 1));
                a.coordinate.x = ((double) (x + 1)) * dx + area.getMin().x;
                a.coordinate.y = ((double) (y + 1)) * dy + area.getMin().y;
                if (isDiag && x % 2 == 0)
                {
                    a.coordinate.y -= dy / 2.0;
                }
                atoms.add(a);
            }
        }
    }

    /**
     * Procedure fills the velocities of the atoms using normal distribution
     */
    public void fillNormalVelocity(double velMean, double velDeviation)
    {
        if (atoms.isEmpty())
        {
            return;
        }
        // set initial velocity
        for (Atom a : atoms)
        {
            double v = rand.nextGaussian() * velDeviation + velMean;
            double om = 2.0 * Math.PI * rand.nextDouble();
            a.velocity.x = v * Math.cos(om);
            a.velocity.y = v * Math.sin(om);
            a.acceleration.x = 0.0;
            a.acceleration.y = 0.0;
        }
    }

    /**
     * Procedure fills the velocities of the atoms to ensure given temperature
     */
    private void fillTemperature(double T)
    {
        double v = Math.sqrt((T * Constants.kB / Constants.EV) * (3.0 / (atomMass * Constants.AK)));
        fillNormalVelocity(v, 0.0);
    }

    /**
     * Procedure calculates the current impulse of this atom set
     */
    public Vector2D calculateImpulse()
    {
        Vector2D res = new Vector2D(0.0, 0.0);
        for (Atom a : atoms)
        {
            res.add(a.velocity);
        }
        return res.prod(atomMass);
    }

    /**
     * Procedure calculates the current kinetic energy of this atom set
     */
    public double calculateKineticEnergy()
    {
        double res = 0.0;
        for (Atom a : atoms)
        {
            res += a.velocity.pow2() / 2.0;
        }
        return atomMass * Constants.EV * Constants.AK * res;
    }

    /**
     * Procedure calculates the average velocity
     */
    public double calculateAverageVelocity()
    {
        double res = 0.0;
        for (Atom a : atoms)
        {
            res += a.velocity.mod() / atoms.size();
        }
        return res;
    }

    /**
     * Procedure calculates the current potential energy of this atom set with respect to given potential
     */
    public double calculatePotentialEnergy()
    {
        double res = 0.0;
        BasePotential pFunc = potential.getFunction();
        double gravityCoeff = gravity * Math.abs(pFunc.getPotentialMin());
        for (int j = 0; j < atoms.size(); j++)
        {
            Vector2D x1 = atoms.get(j).coordinate;
            // add gravity
            res += gravityCoeff * Math.abs(x1.y - area.getMin().y);
            for (int i = j + 1; i < atoms.size(); i++)
            {
                Vector2D x2 = atoms.get(i).coordinate;
                if (Math.abs(x1.x - x2.x) > pFunc.getThreshold() || Math.abs(x1.y - x2.y) > pFunc.getThreshold())
                {
                    continue;
                }
                double r = x1.distance(x2);
                if (r > pFunc.getThreshold())
                {
                    continue;
                }

                res += pFunc.getValue(r);
            }
        }
        return Constants.EV * res;
    }

    /**
     * Procedure calculates and stores current temperature
     */
    private double calculateTemperature()
    {
        double res = 0.0;
        if (!atoms.isEmpty())
        {
            res = 2.0 * eKinetic / (3.0 * Constants.kB * atoms.size());
        }
        return res;
    }

    /**
     * Procedure calculates and stores current physical parameters
     */
    private void calculateParameters()
    {
        eKinetic = calculateKineticEnergy();
        ePotenz = calculatePotentialEnergy();
        temperature = calculateTemperature();
    }

    /**
     * Procedure calculates acceleration for all atoms using their coordinates and velocities.
     */
    public Constants.CalculationType calculateAcceleration()
    {

        Constants.CalculationType retValue = CalculationType.SUCCESS;
        BasePotential pFunc = potential.getFunction();

        // erase previous load
        for (Atom a : atoms)
        {
            a.acceleration.erase();
        }

        // first loop over all other atoms in order to update
        // potential-dependent load
        Vector2D fVec = new Vector2D();
        for (int j = 0; j < atoms.size(); j++)
        {

            if (pauseHandler != null)
            {
                synchronized (pauseHandler)
                {
                    if (pauseHandler.isPaused())
                    {
                        return CalculationType.EXT_INTERRUPT;
                    }
                }
            }

            Atom a1 = atoms.get(j);
            if (a1.acceleration.isNaN())
            {
                retValue = CalculationType.NAN_DETECTED;
                continue;
            }
            Vector2D x1 = a1.coordinate;
            for (int i = j + 1; i < atoms.size(); i++)
            {
                Atom a2 = atoms.get(i);
                Vector2D x2 = a2.coordinate;

                // Checks whether the distance between atoms i and j
                // is smaller than given threshold
                if (Math.abs(x1.x - x2.x) > pFunc.getThreshold() || Math.abs(x1.y - x2.y) > pFunc.getThreshold())
                {
                    continue;
                }
                double r = x1.distance(x2);
                if (r > pFunc.getThreshold())
                {
                    continue;
                }

                // calculate potential-dependent load
                double f = pFunc.getDerivative(r);
                if (Double.isNaN(f))
                {
                    a2.acceleration.invalidate();
                    retValue = CalculationType.NAN_DETECTED;
                    continue;
                }

                // add asymmetric load to both atoms
                fVec.assign(x1);
                fVec.substract(x2);
                fVec.multiply(f);
                // divide to radius shall be separate since it is too small with
                // respect to f
                fVec.divide(r);
                if (Double.isNaN(f))
                {
                    a2.acceleration.invalidate();
                    retValue = CalculationType.NAN_DETECTED;
                    continue;
                }
                a1.acceleration.substract(fVec);
                a2.acceleration.add(fVec);
            }
        }

        // apply gravity and convert to dimensionless form
        Vector2D gVec = new Vector2D(0.0, -1.0 * gravity * Math.abs(pFunc.getPotentialMin()));
        for (Atom a : atoms)
        {
            a.acceleration.add(gVec);
            a.acceleration.divide(atomMass * Constants.AK);
        }

        return retValue;
    }

    /**
     * Procedure validates and removes unstable atoms from the atom set
     */
    private int validateAtoms(boolean remove)
    {
        int nr = 0;
        double vMean = calculateAverageVelocity();
        double areaThreshold = 2.0;
        double velocityThreshold = 1e+3;
        for (int i = 0; i < atoms.size(); )
        {
            Atom a = atoms.get(i);
            boolean isValid = true;
            if (a.acceleration.isNaN())
            {
                // has infinite acceleration
                if (remove)
                {
                    ViewUtils.Debug(this, "removed atom: infinite acceleration");
                }
                isValid = false;
                nr++;
            }
            else if (a.velocity.mod() > velocityThreshold * vMean)
            {
                // has too height velocity
                if (remove)
                {
                    ViewUtils.Debug(this, "removed atom: velocity " + a.velocity.mod() + " greater than " + vMean
                            + "* 1e+3");
                }
                isValid = false;
                nr++;
            }
            else if (a.coordinate.x < areaThreshold * area.getMin().x
                    || a.coordinate.x > areaThreshold * area.getMax().x)
            {
                // is too far outside area
                if (remove)
                {
                    ViewUtils.Debug(this, "removed atom: x-coordinate " + a.coordinate.x
                            + " is too far from area bound");
                }
                isValid = false;
                nr++;
            }
            else if (a.coordinate.y < areaThreshold * area.getMin().y
                    || a.coordinate.y > areaThreshold * area.getMax().y)
            {
                // is too far outside area
                if (remove)
                {
                    ViewUtils.Debug(this, "removed atom: y-coordinate " + a.coordinate.y
                            + " is too far from area bound");
                }
                isValid = false;
                nr++;
            }
            if (!isValid && remove)
            {
                atoms.remove(i);
            }
            else
            {
                i++;
            }
        }
        return nr;
    }

    /**
     * Procedure calculates the next step
     */
    public AtomSet nextStep()
    {
        if (atoms.isEmpty())
        {
            calculateParameters();
            return new AtomSet(this);
        }

        calculationTime = Calendar.getInstance().getTimeInMillis();

        if (solver == null)
        {
            solver = new SolverVerlet();
        }

        // backup current atoms
        backupAtoms();

        // calculate new atom configuration
        double timeStepDevider = 1.0;
        int subStep = 0, maxSubStep = 10;
        while (true)
        {
            Constants.CalculationType t = solver.calculateNextStep(this, Constants.FSec * timeStep / timeStepDevider);
            if (t == CalculationType.EXT_INTERRUPT)
            {
                ViewUtils.Debug(this, "calculation interrupted by external pause");
                restoreAtoms();
                return null;
            }
            int nrInvalid = validateAtoms(false);
            if (nrInvalid == 0)
            {
                break;
            }
            else if (subStep > maxSubStep)
            {
                // solution is invalid and maximum trials number is reached:
                // break
                break;
            }
            else
            {
                // solution is invalid and maximum trials number is yet not
                // reached:
                // try to reduce the time step
                timeStepDevider += 1.0;
                restoreAtoms();
                subStep++;
            }
        }

        // check system stability and boundary conditions
        int nrRemoved = validateAtoms(true);
        if (nrRemoved > 0)
        {
            ViewUtils.Debug(this, "removed " + nrRemoved + " invalid atoms after " + subStep + " substeps");
            calculateParameters();
        }
        else
        {
            // boundary conditions
            for (Atom a : atoms)
            {
                switch (boundaryCondition)
                {
                case REFLECTION:
                    a.reflect(area, atomRadius);
                    break;
                case TRANSLATION:
                    a.translate(area, atomRadius);
                    break;
                }
            }

            // energy normalization
            if (energyNorm != EnergyNormType.NONE)
            {
                double ek1 = eKinetic;
                double e1 = (energyNorm == EnergyNormType.TEMPERATURE) ? ek1 : ek1 + ePotenz;
                calculateParameters();
                double ek2 = eKinetic;
                double e2 = (energyNorm == EnergyNormType.TEMPERATURE) ? ek2 : ek2 + ePotenz;
                double dE = e2 - e1 * (1.0 + thermalChange / 100);
                if (dE != 0.0 && ek2 != 0.0 && dE / ek2 < 1.0)
                {
                    double n = Math.sqrt(1.0 - dE / ek2);
                    for (Atom a : atoms)
                    {
                        a.velocity.multiply(n);
                    }
                    eKinetic -= dE;
                    temperature = calculateTemperature();
                }
            }
            else
            {
                calculateParameters();
            }
        }

        calculationTime = Calendar.getInstance().getTimeInMillis() - calculationTime;
        return new AtomSet(this);
    }

    /**
     * Procedure backups current atoms into the backupedAtoms vector
     */
    private void backupAtoms()
    {
        if (atoms.size() == backupedAtoms.size())
        {
            for (int i = 0; i < atoms.size(); i++)
            {
                backupedAtoms.get(i).assign(atoms.get(i));
            }
        }
        else
        {
            backupedAtoms.clear();
            for (Atom a : atoms)
            {
                backupedAtoms.add(new Atom(a));
            }
        }
    }

    /**
     * Procedure restores the backupedAtoms into the atoms vector
     */
    private void restoreAtoms()
    {
        for (int i = 0; i < atoms.size(); i++)
        {
            atoms.get(i).assign(backupedAtoms.get(i));
        }
    }

    /**
     * Procedure reads the atom parameters from shared preferences
     */
    public void readParameters(Context context)
    {
        ViewUtils.Debug(this, "reading parameters from shared preferences");

        Resources resources = context.getResources();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // Potential
        potential.setType(PotentialType.valueOf(pref.getString(SettingsActivity.KEY_POTENTIAL, context.getResources()
                .getString(R.string.pref_potential_default))));

        // Physical area
        area.set(
                Double.parseDouble(pref.getString(SettingsActivity.KEY_AREA_MIN_X,
                        resources.getString(R.string.pref_area_min_x_default))),
                Double.parseDouble(pref.getString(SettingsActivity.KEY_AREA_MAX_X,
                        resources.getString(R.string.pref_area_max_x_default))),
                Double.parseDouble(pref.getString(SettingsActivity.KEY_AREA_MIN_Y,
                        resources.getString(R.string.pref_area_min_y_default))),
                Double.parseDouble(pref.getString(SettingsActivity.KEY_AREA_MAX_Y,
                        resources.getString(R.string.pref_area_max_y_default))));
        viewPort.assign(area);
        viewPort.scale(
                area,
                Double.parseDouble(pref.getString(SettingsActivity.KEY_AREA_ZOOM,
                        resources.getString(R.string.pref_area_zoom_default))),
                Double.parseDouble(resources.getString(R.string.pref_area_zoom_max)),
                pref.getFloat(SettingsActivity.KEY_AREA_SCROLL_X, 0),
                pref.getFloat(SettingsActivity.KEY_AREA_SCROLL_Y, 0));

        // Initial layout
        if (pref.getBoolean(SettingsActivity.KEY_REBUILD, true) || atoms.isEmpty())
        {
            int methodIndex = SettingsActivity.getListPreferenceIndex(context, SettingsActivity.KEY_FILL_METHODS,
                    R.string.pref_fill_methods_default, R.array.pref_fill_methods_values);
            Integer nX = Integer.valueOf(pref.getString(SettingsActivity.KEY_GRID_HOR_DIMENSION,
                    resources.getString(R.string.pref_grid_hor_dimension_default)));
            Integer nY = Integer.valueOf(pref.getString(SettingsActivity.KEY_GRID_VER_DIMENSION,
                    resources.getString(R.string.pref_grid_ver_dimension_default)));
            fillGridCoordinates(nX.intValue(), nY.intValue(), methodIndex == 0);
        }

        atomRadius = Double.parseDouble(pref.getString(SettingsActivity.KEY_ATOM_RADIUS,
                resources.getString(R.string.pref_atom_radius_default)));
        atomMass = Double.parseDouble(pref.getString(SettingsActivity.KEY_ATOM_MASS,
                resources.getString(R.string.pref_atom_mass_default)));
        atomImage = pref
                .getString(SettingsActivity.KEY_ATOM_SKIN, resources.getString(R.string.pref_atom_skin_default));
        gravity = Double.parseDouble(pref.getString(SettingsActivity.KEY_BOUND_GRAVITY,
                resources.getString(R.string.pref_bound_gravity_default)));
        thermalChange = Double.parseDouble(pref.getString(SettingsActivity.KEY_BOUND_THERMAL_CHANGE,
                resources.getString(R.string.pref_bound_thermal_change_default)));
        timeStep = Double.parseDouble(pref.getString(SettingsActivity.KEY_CALC_TIME_STEP,
                resources.getString(R.string.pref_calc_time_step_default)));

        // temperature shall be filled after atomMass is set since atom mass is
        // used for temperature calculations
        fillTemperature(Double.parseDouble(pref.getString(SettingsActivity.KEY_INIT_TEMPERATURE,
                resources.getString(R.string.pref_init_temperature_default))));
        {
            int energyNormIndex = SettingsActivity.getListPreferenceIndex(context, SettingsActivity.KEY_ENERGY_NORM,
                    R.string.pref_energy_norm_default, R.array.pref_energy_norm_values);
            if (energyNormIndex >= 0 && energyNormIndex < EnergyNormType.values().length)
            {
                energyNorm = EnergyNormType.values()[energyNormIndex];
            }
        }

        // update parameters and delete atoms that are outside of the area
        calculateParameters();
        for (int i = 0; i < atoms.size(); )
        {
            if (!area.isInside(atoms.get(i).coordinate))
            {
                atoms.remove(i);
            }
            else
            {
                i++;
            }
        }
    }

    /**
     * Procedure writes the atom parameters into shared preferences
     */
    public void writeParameters(Context context)
    {
        ViewUtils.Debug(this, "writing parameters into shared preferences");

        Resources r = context.getResources();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(SettingsActivity.KEY_AREA_ZOOM,
                CompatUtils.getDecimalFormat(r.getString(R.string.pref_area_zoom_format)).format(viewPort.getZoom()));
        prefEditor.putFloat(SettingsActivity.KEY_AREA_SCROLL_X, (float) viewPort.getFocus().x);
        prefEditor.putFloat(SettingsActivity.KEY_AREA_SCROLL_Y, (float) viewPort.getFocus().y);
        prefEditor.putString(SettingsActivity.KEY_BOUND_GRAVITY,
                CompatUtils.getDecimalFormat(r.getString(R.string.pref_bound_gravity_value_format)).format(gravity));
        prefEditor.putString(
                SettingsActivity.KEY_BOUND_THERMAL_CHANGE,
                CompatUtils.getDecimalFormat(r.getString(R.string.pref_bound_thermal_change_value_format)).format(
                        thermalChange));
        prefEditor.putString(SettingsActivity.KEY_CALC_TIME_STEP,
                CompatUtils.getDecimalFormat(r.getString(R.string.pref_calc_time_step_value_format)).format(timeStep));
        prefEditor.putString(SettingsActivity.KEY_INIT_TEMPERATURE, String.valueOf((int) temperature));
        TypedArray energyNormValues = r.obtainTypedArray(R.array.pref_energy_norm_values);
        prefEditor.putString(SettingsActivity.KEY_ENERGY_NORM, energyNormValues.getString(energyNorm.value()));
        energyNormValues.recycle();
        prefEditor.commit();
    }

    /**
     * Procedure rotates atom set accordingly to the sensor data
     */
    public void rotate(int previousRotation, int currentRotation)
    {
        for (Atom a : atoms)
        {
            a.coordinate.rotate(previousRotation, currentRotation);
            a.velocity.rotate(previousRotation, currentRotation);
            a.acceleration.rotate(previousRotation, currentRotation);
        }
    }

    /**
     * Procedure applies new scaling parameters for the viewport
     */
    public void scale(double scaleFactor, double maxScale, double dx, double dy)
    {
        viewPort.scale(area, scaleFactor, maxScale, dx, dy);
    }

    /**
     * Procedure returns the last calculation duration
     */
    public long getCalculationTime()
    {
        return calculationTime;
    }

}
