package api.domain.exames;

import api.domain.ValidacaoException;
import api.domain.exames.agendamento.ValidadorAgendamentoDeExame;
import api.domain.medico.Medico;
import api.domain.medico.MedicoRepository;
import api.domain.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaDeExames {

    @Autowired
    private ExameRepository exameRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private List<ValidadorAgendamentoDeExame> validadores;

    public DadosDetalhamentoExame agendar(DadosAgendamentoExame dados) {
        if (!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente informado não existe!");
        }

        if (dados.idMedico() != null && !medicoRepository.existsById(dados.idMedico())) {
            throw new ValidacaoException("Id do médico informado não existe!");
        }

        validadores.forEach(v -> v.validar(dados));

        var paciente = pacienteRepository.findById(dados.idPaciente())
                .orElseThrow(() -> new ValidacaoException("Paciente não encontrado!"));

        var medico = escolherMedico(dados);
        if (medico == null) {
            throw new ValidacaoException("Não existe médico disponível nessa data!");
        }

        var exame = new Exame(null, medico, paciente, dados.data());
        exameRepository.save(exame);

        return new DadosDetalhamentoExame(exame);
    }

    private Medico escolherMedico(DadosAgendamentoExame dados) {
        if (dados == null || dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é obrigatória quando médico não for escolhido!");
        }

        if (dados.idMedico() != null) {
            return medicoRepository.findById(dados.idMedico())
                    .orElseThrow(() -> new ValidacaoException("Médico não encontrado!"));
        }

        return medicoRepository.escolherMedicoAleatorioLivreNaData(dados.especialidade(), dados.data());
    }

}

