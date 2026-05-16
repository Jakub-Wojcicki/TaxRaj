package pl.taxraj.taxraj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaxrajApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxrajApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner testData(
			pl.taxraj.taxraj.repository.UzytkownikRepository uzytkownikRepo,
			pl.taxraj.taxraj.repository.KlientRepository klientRepo,
			pl.taxraj.taxraj.repository.FakturaRepository fakturaRepo,
			pl.taxraj.taxraj.repository.KontrahentRepository kontrahentRepo,
			org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
		return args -> {
			if (uzytkownikRepo.count() == 0) {
				pl.taxraj.taxraj.model.Uzytkownik anna = uzytkownikRepo.save(new pl.taxraj.taxraj.model.Uzytkownik(
						"Anna", "Kowalska", "anna@tax-raj.pl",
						passwordEncoder.encode("haslo123"), "KSIEGOWY"
				));

				pl.taxraj.taxraj.model.Klient abc = klientRepo.save(new pl.taxraj.taxraj.model.Klient(
						"ABC Sp. z o.o.", "1234567890", "ul. Testowa 1", "abc@firma.pl", "123456789"
				));
				pl.taxraj.taxraj.model.Klient xyz = klientRepo.save(new pl.taxraj.taxraj.model.Klient(
						"XYZ Transport", "9876543210", "ul. Logistyczna 5", "biuro@xyz.pl", "987654321"
				));
				pl.taxraj.taxraj.model.Klient kowal = klientRepo.save(new pl.taxraj.taxraj.model.Klient(
						"Jan Nowak", "5556667770", "ul. Kwiatowa 12", "jan@nowak.pl", "555666777"
				));

				// Testowe faktury
				pl.taxraj.taxraj.model.Faktura f1 = new pl.taxraj.taxraj.model.Faktura();
				f1.setNumer("FV/2026/05/001");
				f1.setTyp(pl.taxraj.taxraj.model.enums.TypFaktury.SPRZEDAZOWA);
				f1.setStatus(pl.taxraj.taxraj.model.enums.StatusFaktury.ZAPLACONA);
				f1.setDataWystawienia(java.time.LocalDate.of(2026, 5, 12));
				f1.setDataPlatnosci(java.time.LocalDate.of(2026, 5, 26));
				f1.setKwotaNetto(new java.math.BigDecimal("4000.00"));
				f1.setKwotaVat(new java.math.BigDecimal("920.00"));
				f1.setKwotaBrutto(new java.math.BigDecimal("4920.00"));
				f1.setWaluta("PLN");
				f1.setKlient(abc);
				f1.setUzytkownik(anna);
				fakturaRepo.save(f1);

				pl.taxraj.taxraj.model.Faktura f2 = new pl.taxraj.taxraj.model.Faktura();
				f2.setNumer("FV/2026/05/002");
				f2.setTyp(pl.taxraj.taxraj.model.enums.TypFaktury.SPRZEDAZOWA);
				f2.setStatus(pl.taxraj.taxraj.model.enums.StatusFaktury.NIEZAPLACONA);
				f2.setDataWystawienia(java.time.LocalDate.of(2026, 5, 11));
				f2.setDataPlatnosci(java.time.LocalDate.of(2026, 5, 25));
				f2.setKwotaNetto(new java.math.BigDecimal("600.00"));
				f2.setKwotaVat(new java.math.BigDecimal("138.00"));
				f2.setKwotaBrutto(new java.math.BigDecimal("738.00"));
				f2.setWaluta("PLN");
				f2.setKlient(kowal);
				f2.setUzytkownik(anna);
				fakturaRepo.save(f2);

				pl.taxraj.taxraj.model.Faktura f3 = new pl.taxraj.taxraj.model.Faktura();
				f3.setNumer("FV/2026/05/003");
				f3.setTyp(pl.taxraj.taxraj.model.enums.TypFaktury.SPRZEDAZOWA);
				f3.setStatus(pl.taxraj.taxraj.model.enums.StatusFaktury.CZESCIOWO_ZAPLACONA);
				f3.setDataWystawienia(java.time.LocalDate.of(2026, 5, 10));
				f3.setDataPlatnosci(java.time.LocalDate.of(2026, 5, 24));
				f3.setKwotaNetto(new java.math.BigDecimal("1800.00"));
				f3.setKwotaVat(new java.math.BigDecimal("414.00"));
				f3.setKwotaBrutto(new java.math.BigDecimal("2214.00"));
				f3.setWaluta("PLN");
				f3.setKlient(xyz);
				f3.setUzytkownik(anna);
				fakturaRepo.save(f3);

				pl.taxraj.taxraj.model.Faktura f4 = new pl.taxraj.taxraj.model.Faktura();
				f4.setNumer("FK/2026/05/001");
				f4.setTyp(pl.taxraj.taxraj.model.enums.TypFaktury.KOSZTOWA);
				f4.setStatus(pl.taxraj.taxraj.model.enums.StatusFaktury.ZAPLACONA);
				f4.setDataWystawienia(java.time.LocalDate.of(2026, 5, 7));
				f4.setKwotaNetto(new java.math.BigDecimal("1000.00"));
				f4.setKwotaVat(new java.math.BigDecimal("230.00"));
				f4.setKwotaBrutto(new java.math.BigDecimal("1230.00"));
				f4.setWaluta("PLN");
				f4.setKlient(abc);
				f4.setUzytkownik(anna);
				fakturaRepo.save(f4);

				kontrahentRepo.save(new pl.taxraj.taxraj.model.Kontrahent(
						"Biuro Materiałów Sp. z o.o.", "1112223334",
						"ul. Magazynowa 5, Warszawa", "kontakt@materialy.pl", "224445566"
				));
				kontrahentRepo.save(new pl.taxraj.taxraj.model.Kontrahent(
						"Hurtownia Papier-Tech", "5556667778",
						"ul. Hurtowa 12, Kraków", "biuro@papiertech.pl", "126667788"
				));
				kontrahentRepo.save(new pl.taxraj.taxraj.model.Kontrahent(
						"Software House Pro", "9998887776",
						"ul. Cyfrowa 7, Wrocław", "hello@swhouse.pl", "717778899"
				));

				System.out.println("✅ Dodano testowych użytkowników, klientów i faktury!");
			}
		};
	}
}
